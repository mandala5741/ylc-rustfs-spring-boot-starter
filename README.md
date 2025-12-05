## 独立的对象存储 Spring Boot Starter

https://blog.csdn.net/weimeilayer/article/details/149197572?spm=1011.2415.3001.5331

## 引入依赖
```xml
<dependency>
    <groupId>io.github.mandala5741</groupId>
    <artifactId>ylc-rustfs-spring-boot-starter</artifactId>
    <version>1.0.2</version>
</dependency>
```
## x86镜像
```bash
docker pull registry.cn-hangzhou.aliyuncs.com/qiluo-images/rustfs:latest
```
## arm架构镜像
```bash
docker pull registry.cn-hangzhou.aliyuncs.com/qiluo-images/linux_arm64_rustfs:latest
```
运行程序
```bash
docker run -d \              # 后台运行
  --name rustfs \           # 容器名称
  --restart=always \        # 自动重启
  --privileged=true \       # 特权模式
  -p 9000:9000 \           # API端口
  -p 9001:9001 \           # Web控制台端口
  -e RUSTFS_ACCESS_KEY=rustfsadmin \   # 访问密钥
  -e RUSTFS_SECRET_KEY=rustfsadmin \   # 秘密密钥
  -v /data/rustfs/data:/data \        # 数据持久化
  registry.cn-hangzhou.aliyuncs.com/qiluo-images/rustfs:latest  # 镜像
```
# 部署脚本

```bash
#!/bin/bash
# deploy-rustfs-production.sh

set -e  # 遇到错误退出

echo "=== RustFS生产环境部署 ==="

# 配置参数
CONTAINER_NAME="rustfs"
API_PORT="9000"
CONSOLE_PORT="9001"
DATA_DIR="/data/rustfs"
IMAGE="registry.cn-hangzhou.aliyuncs.com/qiluo-images/rustfs:latest"

# 1. 生成强密码
echo "1. 生成安全密钥..."
ACCESS_KEY=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | head -c 20)
SECRET_KEY=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | head -c 40)

# 保存密钥到文件（安全）
KEY_FILE="$DATA_DIR/.rustfs-keys"
mkdir -p $DATA_DIR
echo "RUSTFS_ACCESS_KEY=$ACCESS_KEY" > $KEY_FILE
echo "RUSTFS_SECRET_KEY=$SECRET_KEY" >> $KEY_FILE
chmod 600 $KEY_FILE

echo "密钥已保存到: $KEY_FILE"
echo "访问密钥: $ACCESS_KEY"
echo "秘密密钥: $SECRET_KEY"

# 2. 清理旧容器
echo "2. 清理旧容器..."
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

# 3. 设置数据目录权限
echo "3. 设置数据目录权限..."
sudo mkdir -p $DATA_DIR
sudo chown -R 1000:1000 $DATA_DIR
sudo chmod -R 777 $DATA_DIR

# 4. 拉取镜像
echo "4. 拉取镜像..."
docker pull $IMAGE

# 5. 部署容器
echo "5. 部署容器..."
docker run -d \
  --name $CONTAINER_NAME \
  --restart=always \
  --privileged=true \
  -p $API_PORT:9000 \
  -p $CONSOLE_PORT:9001 \
  -v $DATA_DIR:/data \
  -e RUSTFS_ACCESS_KEY=$ACCESS_KEY \
  -e RUSTFS_SECRET_KEY=$SECRET_KEY \
  $IMAGE

# 6. 等待并验证
echo "6. 验证部署..."
sleep 5

# 检查容器状态
if docker ps | grep -q $CONTAINER_NAME; then
    echo "✅ 容器运行正常"
    
    # 检查日志
    LOG_OUTPUT=$(docker logs --tail 10 $CONTAINER_NAME)
    echo "容器日志:"
    echo "$LOG_OUTPUT"
    
    # 测试连接
    if curl -s http://localhost:$API_PORT > /dev/null 2>&1; then
        echo "✅ API服务可访问"
    else
        echo "⚠️  API服务不可访问，请检查防火墙"
    fi
else
    echo "❌ 容器启动失败"
    docker logs $CONTAINER_NAME
    exit 1
fi

echo ""
echo "=== 部署完成 ==="
echo "📊 服务信息:"
echo "  API端点: http://$(hostname -I | awk '{print $1}'):$API_PORT"
echo "           http://localhost:$API_PORT"
echo "  Web控制台: http://$(hostname -I | awk '{print $1}'):$CONSOLE_PORT/rustfs/console/index.html"
echo "             http://localhost:$CONSOLE_PORT/rustfs/console/index.html"
echo "  Access Key: $ACCESS_KEY"
echo "  Secret Key: $SECRET_KEY"
echo ""
echo "🔧 管理命令:"
echo "  查看状态: docker ps | grep $CONTAINER_NAME"
echo "  查看日志: docker logs -f $CONTAINER_NAME"
echo "  进入容器: docker exec -it $CONTAINER_NAME sh"
echo "  停止服务: docker stop $CONTAINER_NAME"
echo "  重启服务: docker restart $CONTAINER_NAME"
echo "  卸载服务: docker stop $CONTAINER_NAME && docker rm $CONTAINER_NAME"
echo ""
echo "💾 密钥备份:"
echo "  密钥文件: $KEY_FILE"
echo "  请妥善保管密钥！"

```


##  PGSQL 创建表
```sql
CREATE TABLE "public"."sys_file" (
"id" "pg_catalog"."varchar" COLLATE "pg_catalog"."default" NOT NULL,
"name" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"group_id" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"file_type" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"suffix" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"size" "pg_catalog"."int4",
"preview_url" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"storage_type" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"storage_url" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"bucket_name" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"object_name" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"visit_count" "pg_catalog"."int4",
"sort" "pg_catalog"."int4",
"remarks" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"gmt_create" "pg_catalog"."timestamp",
"gmt_modified" "pg_catalog"."timestamp",
"create_by" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"update_by" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"del_flag" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
"tenant_id" "pg_catalog"."int4",
"original" "pg_catalog"."varchar" COLLATE "pg_catalog"."default",
CONSTRAINT "sys_file_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON COLUMN "public"."sys_file"."id" IS '主键';
COMMENT ON COLUMN "public"."sys_file"."name" IS '原文件名';
COMMENT ON COLUMN "public"."sys_file"."group_id" IS '分组编号，对应多文件';
COMMENT ON COLUMN "public"."sys_file"."file_type" IS '文件类型';
COMMENT ON COLUMN "public"."sys_file"."suffix" IS '文件后缀';
COMMENT ON COLUMN "public"."sys_file"."size" IS '文件大小，单位字节';
COMMENT ON COLUMN "public"."sys_file"."preview_url" IS '预览地址';
COMMENT ON COLUMN "public"."sys_file"."storage_type" IS '存储类型';
COMMENT ON COLUMN "public"."sys_file"."storage_url" IS '存储地址';
COMMENT ON COLUMN "public"."sys_file"."bucket_name" IS '桶名';
COMMENT ON COLUMN "public"."sys_file"."object_name" IS '桶内文件名';
COMMENT ON COLUMN "public"."sys_file"."visit_count" IS '访问次数';
COMMENT ON COLUMN "public"."sys_file"."sort" IS '排序值';
COMMENT ON COLUMN "public"."sys_file"."remarks" IS '备注';
COMMENT ON COLUMN "public"."sys_file"."gmt_create" IS '创建时间';
COMMENT ON COLUMN "public"."sys_file"."gmt_modified" IS '更新时间';
COMMENT ON COLUMN "public"."sys_file"."create_by" IS '创建人ID';
COMMENT ON COLUMN "public"."sys_file"."update_by" IS '修改人ID';
COMMENT ON COLUMN "public"."sys_file"."del_flag" IS '逻辑删除（0：未删除；null：已删除）';
COMMENT ON COLUMN "public"."sys_file"."tenant_id" IS '所属租户';
COMMENT ON COLUMN "public"."sys_file"."original" IS '原始文件名';
COMMENT ON TABLE "public"."sys_file" IS '系统基本信息--文件管理信息表';
```


# yaml 配置文件
minio:
  endpoint: ENC(2ibwJTJtC9aSCwI+REN4up/bkWiPjWYei0XXqXv9dsD80cEkQ3BBbQ==)
  access-key: ENC(UbfMrajSAkV2JMRqVJdZTxwmQotPjhp9RZBjJ6ocd/4=)
  secret-key: ENC(ErfPLkmb/e6Bkq+4Yv9L/BnWkVmTtsMFnY03v0GgK9+LIbfZTcd0d2+6J8Pm5HJt)
  bucket-name: ENC(KWMCzT4HsuQ3owNp6xQs53qekQFGlFfmW8YLiz6g0ns=)
  public-bucket-name: ENC(NHW2QW2iwf2YEWtiC95nf3gK4UDvwobBUQAB6nHfaPw=)
  preview-domain: ENC(F1qPbzbrpnpftyLw1TENQ9aMzNVGX269TQLXtqiEKqYBZ7XUx1aQPQ==)


# 配置结构说明
1. MinIO 连接配置
   yaml
   minio:
   endpoint:       # MinIO 服务器地址（加密存储）
   access-key:     # 访问密钥（加密存储）
   secret-key:     # 秘密密钥（加密存储）
2. 存储桶配置
   yaml
   bucket-name:             # 默认存储桶（通常用于私有文件）
   public-bucket-name:      # 公开访问存储桶（通常用于公共文件）
   preview-domain:         # 文件预览域名（可能包含CDN地址）
### 配置项详细说明
   配置项	说明	示例值（解密后）
   endpoint	MinIO 服务地址	http://minio.example.com:9000
   access-key	访问密钥 ID	AKIAIOSFODNN7EXAMPLE
   secret-key	秘密访问密钥	wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
   bucket-name	默认存储桶名称	private-bucket
   public-bucket-name	公开存储桶名称	public-bucket
   preview-domain	文件访问域名	https://cdn.example.com
