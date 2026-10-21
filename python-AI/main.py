import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI

load_dotenv(override=True)
llm_deepseek=ChatOpenAI(
    base_url=os.getenv("DEEPSEEK_BASE_URL"),
    api_key=os.getenv("DEEPSEEK_API_KEY"),
    model="deepseek-v4-pro",
)

def keep_recent_messages(messages,max_pairs=3):
    system_messages=[m for m in messages if m.get("role")=="system"]
    conversation_messages=[m for m in messages if m.get("role")!="system"]
    recent_messages=conversation_messages[-(max_pairs*2):]
    return system_messages+recent_messages

i=1
print("输入问题，当输入quit时退出。")
messages=[
    {"role": "system","content":"你是一个友善的智能聊天机器人"}
]
while True:

    print("\n",f"第{i}轮对话：","\n")

    user_input=input("请输入问题：")

    if user_input=="quit":
        print("欢迎下次再来。")
        break

    #将用户问题加入消息列表
    messages.append({"role":"user","content":user_input})

    reply_content=""
    #优化历史记录，最多保存十轮
    recent_messages=keep_recent_messages(messages,max_pairs=10)
    for chunk in llm_deepseek.stream(recent_messages):
        if chunk.content:
            print(chunk.content,end="",flush=True)
            reply_content+=chunk.content

    #将AI回复加入消息列表
    messages.append({"role":"assistant","content":reply_content})
    i+=1