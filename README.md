# 网上书店 Online Bookstore

一个前后端分离的网上书店系统,带基于 RAG 的智能客服助手。

## 功能

- **用户端**:注册登录、图书浏览与搜索、购物车、下单购买、订单跟踪、图书评价
- **管理后台**:图书 / 订单 / 用户 / 评价管理,销售数据报表
- **智能客服**:基于 RAG 的知识库问答(配送、退换货、会员政策、图书导读)+ 实时数据库查询(销量榜、价格、库存)

## 技术栈

| 模块 | 目录 | 技术 |
|---|---|---|
| 前端 | `bookstore-frontend/` | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router + ECharts |
| 后端 | `bookstore-backend/` | Spring Boot 3.2 + MyBatis-Plus + Spring Security(JWT)+ MySQL |
| 智能客服 | `python-AI/` | FastAPI + LangChain + DeepSeek + ChromaDB + sentence-transformers |

## 目录结构

```
online-bookstore/
├── README.md
├── bookstore.sql          # 数据库初始化脚本(建库 + 建表 + 种子数据)
├── bookstore-backend/     # Spring Boot 后端(端口 8080)
├── bookstore-frontend/    # Vue 前端(端口 3000)
└── python-AI/             # FastAPI + RAG 智能客服(端口 8000)
```

## 前置依赖

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Python 3.11+
- MySQL 8

## 启动步骤

### 1. 数据库

```bash
mysql -u root -p < bookstore.sql
```

脚本会创建数据库 `bookstore_new` 并写入表结构与示例数据。

### 2. 后端(端口 8080)

需要先设置环境变量(无默认值的变量必须提供):

```bash
# Windows PowerShell
$env:DB_HOST="localhost"; $env:DB_PORT="3306"; $env:DB_NAME="bookstore_new"
$env:DB_USERNAME="root"; $env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="至少32字符的随机字符串"

# Linux/macOS
export DB_HOST=localhost DB_PORT=3306 DB_NAME=bookstore_new
export DB_USERNAME=root DB_PASSWORD=你的数据库密码
export JWT_SECRET=至少32字符的随机字符串

cd bookstore-backend
mvn spring-boot:run
```

### 3. 智能客服(端口 8000)

```bash
cd python-AI
# 安装依赖(建议用虚拟环境)
pip install -r requirements.txt

# 配置密钥:复制模板并填入 DeepSeek API Key 与数据库连接
cp .env.example .env      # 然后编辑 .env

python api.py
```

> 首次启动会从 HuggingFace 下载两个本地模型(embedding + rerank,合计约 840MB),需要联网且耗时较长。下载后可复用缓存。
> 启动时会自动读取 `knowledge/*.md` 建立向量索引(基于文件哈希,内容不变则跳过)。

### 4. 前端(端口 3000)

```bash
cd bookstore-frontend
npm install
npm run dev
```

开发模式下 Vite 已配置代理:`/api/chat` → 8000(智能客服),`/api` → 8080(后端)。

打开 http://localhost:3000 即可访问。

## 默认端口

| 服务 | 端口 |
|---|---|
| 前端 | 3000 |
| 后端 | 8080 |
| 智能客服 | 8000 |
| MySQL | 3306 |

## 环境变量

**后端**(`bookstore-backend`):

| 变量 | 说明 | 默认值 |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 数据库连接 | localhost / 3306 / bookstore_new |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | root / 1234 |
| `JWT_SECRET` | JWT 签名密钥(**必填**,≥32 字符) | 无 |
| `CORS_ORIGINS` | 允许的前端来源 | http://localhost:3000 |

**智能客服**(`python-AI/.env`):见 `python-AI/.env.example`,需填 `DEEPSEEK_BASE_URL`、`DEEPSEEK_API_KEY` 与数据库连接。

> ⚠️ `.env` 与 `application-prod.yml` 均含敏感信息,已被 `.gitignore` 忽略,请勿提交。
