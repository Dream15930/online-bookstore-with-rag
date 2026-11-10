# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **主要受众：招聘方（面试官 / 技术负责人）。** 在浏览简历、作品集或面试演示的场景中，用几分钟快速评估开发者的全栈与 AI 集成能力。他们要的是"能跑起来、看得懂、有深度"的项目。
- **产品内描绘的终端用户：** 网上书店会员（浏览、搜索、下单、跟踪订单、评价）与管理员（图书 / 订单 / 用户 / 评价管理、销售报表）。

## Product Purpose

一个前后端分离的完整网上书店系统，附带基于 RAG 的智能客服，作为**求职作品集**展示完整的全栈 + AI 工程能力。

成功 = 面试官能在几分钟内跑通并看懂：标准电商闭环（浏览→购物车→下单→订单→评价 + 管理后台报表）完整可用，且有一处（RAG 客服）体现真实的 AI 集成深度。

## Positioning

不是"又一个电商 demo"。它在标准电商闭环之上，集成了一条**真实可用**的 RAG 客服链路：向量检索知识库（配送 / 退换货 / 会员政策 / 图书导读）+ 对业务库的**只读工具调用**（销量榜、价格、库存、分类统计、店铺概览），经 DeepSeek 编排后通过 SSE 流式返回。

差异化在于把「Vue 前端 ↔ Spring Boot API ↔ Python LLM 工具调用」**整条链路打通**，而非单点演示某个模型或某个页面。

## Operating Context

- **本地开发运行**，四个服务：前端 3000 / 后端 8080 / 智能客服 8000 / MySQL 3306。Vite 代理 `/api`→8080、`/api/chat`→8000。
- 演示与评估通常发生在本地启动后手动走一遍主要流程；面试中可能被要求现场讲解或运行。
- 客服首次启动需联网下载本地模型（embedding + rerank，约 840MB），需在演示前预热。

## Capabilities and Constraints

**用户端：** 注册登录（JWT）、图书浏览与搜索、分类筛选、购物车、结算下单、订单跟踪、图书评价、个人资料与收货地址。

**管理后台：** 仪表盘、图书 / 订单 / 用户 / 评价管理、销售数据报表（ECharts）。

**智能客服「书虫小助手」：** 全局悬浮窗，SSE 流式返回；RAG（ChromaDB + 本地 `text2vec-base-chinese` embedding + mmarco rerank）+ DeepSeek LLM + 只读数据库工具 + 知识库检索。

**角色：** `member` / `admin` / `super_admin`。

**约束：**
- 技术栈**固定**，不作为可替换项：Spring Boot 3.2 + MyBatis-Plus + Spring Security(JWT) + MySQL 8；Vue 3 + Vite 5 + Element Plus + Pinia + ECharts；FastAPI + LangChain + DeepSeek + ChromaDB。
- 无 Redis / 缓存层（是否引入未定）。

**术语：** 站点名「网上书店 / BookStore」，客服名「书虫小助手」。

## Brand Commitments

现有名称与文案（**存在，但用户未标记为必须保留，后续文案可优化**）：
- 站点名：网上书店 / BookStore
- 标语：发现好书，畅享阅读
- 首页 hero：发现你的下一本好书 / 海量精选图书·正版保障·即刻阅读
- 客服：书虫小助手 · 智能客服·在线
- 无图片 Logo，使用图标（Notebook 图标）+ 文字构成 Logo。

## Evidence on Hand

- `bookstore.sql`：建库 `bookstore_new` + 8 张表 + 种子数据（5 个分类、10 本图书含书名/作者/ISBN/价格/库存/分类，封面 URL 为空由前端 CSS 生成；用户 admin / testuser，密码 123456）。
- `python-AI/knowledge/policies.md`、`book_guides.md`：客服知识库文本。

**内容均为占位。** 书籍、封面、用户、评价等皆为演示数据，后续可能替换为真实内容。**不得虚构**真实客户、销量、认证、媒体报道或授权。

## Product Principles

1. **完整闭环优先。** 任何界面或功能都应支撑"浏览→下单→管理"这条可演示的完整链路，而不是孤立的漂亮页面。
2. **可被快速评估。** 作品集的第一要务是几分钟内被看懂、跑通；结构与命名清晰优先于炫技。
3. **AI 集成要真实。** 智能客服体现的是"LLM + 工具调用 + 向量检索"的真实链路，不靠假数据或硬编码回复撑场面。
4. **占位内容可替换。** 设计时假定文案与数据会更换，不把演示数据写死进界面逻辑。
5. **技术栈既定。** 在 Spring Boot + Vue + Python 的既定栈内把事做好，不引入新框架。
