# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
}

# ECS Namespace for Service Connect (Discovery)
resource "aws_service_discovery_http_namespace" "main" {
  name        = "bizflow"
  description = "Service discovery namespace for BizFlow microservices"
}

# IAM Role for ECS Task Execution (Pulling images, pushing logs)
resource "aws_iam_role" "ecs_execution" {
  name = "${var.project_name}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# IAM Role for ECS Tasks (Permissions for application code itself)
resource "aws_iam_role" "ecs_task" {
  name = "${var.project_name}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

# Attach basic CloudWatch and KMS/Secrets permissions to ECS task role
resource "aws_iam_policy" "ecs_task_policy" {
  name        = "${var.project_name}-ecs-task-policy"
  description = "Policy for BizFlow ECS tasks"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameters",
          "secretsmanager:GetSecretValue",
          "kms:Decrypt"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_attachment" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = aws_iam_policy.ecs_task_policy.arn
}

# CloudWatch Log Group for ECS Services
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project_name}"
  retention_in_days = 7
}

# Account ID lookup helper
data "aws_caller_identity" "current" {}

# ==========================================
# MICROSERVICES DEFINITION LOOP
# ==========================================

locals {
  # Common Env variables for Java Spring Boot apps
  common_db_env = [
    { name = "DB_HOST", value = element(split(":", aws_db_instance.mysql.endpoint), 0) },
    { name = "DB_PORT", value = "3306" },
    { name = "DB_USER", value = aws_db_instance.mysql.username },
    { name = "DB_PASSWORD", value = var.db_password },
    { name = "REDIS_HOST", value = aws_elasticache_replication_group.redis.primary_endpoint_address },
    { name = "SPRING_JPA_HIBERNATE_DDL_AUTO", value = "validate" }
  ]

  common_mq_env = [
    { name = "RABBITMQ_HOST", value = element(split(":", element(split("//", aws_mq_broker.rabbitmq.instances[0].endpoints[0]), 1)), 0) },
    { name = "RABBITMQ_PORT", value = "5671" }, # SSL Port
    { name = "RABBITMQ_USERNAME", value = "bizflow" },
    { name = "RABBITMQ_PASSWORD", value = var.db_password }
  ]

  common_kafka_env = [
    { name = "KAFKA_BOOTSTRAP_SERVERS", value = "localhost:9092" } # Fallback or configuration
  ]

  services = {
    # 1. API GATEWAY
    gateway = {
      port      = 8000
      cpu       = 256
      mem       = 512
      mount_efs = false
      env = concat(
        local.common_db_env,
        [
          { name = "PROMOTION_SERVICE_URL", value = "http://promotion-service:8082" },
          { name = "AUTH_SERVICE_URL", value = "http://authentication-service:8086" },
          { name = "ADMIN_USER_SERVICE_URL", value = "http://admin-user-service:8201" },
          { name = "ADMIN_PRODUCT_SERVICE_URL", value = "http://admin-product-service:8204" },
          { name = "ADMIN_ORDER_SERVICE_URL", value = "http://admin-order-service:8203" },
          { name = "ADMIN_REPORT_SERVICE_URL", value = "http://admin-report-service:8205" },
          { name = "ADMIN_HOME_SERVICE_URL", value = "http://admin-home-service:8200" },
          { name = "SALES_SERVICE_URL", value = "http://sales-service:8081" },
          { name = "CATALOG_SERVICE_URL", value = "http://catalog-service:8083" },
          { name = "INVENTORY_SERVICE_URL", value = "http://inventory-service:8084" },
          { name = "CUSTOMER_SERVICE_URL", value = "http://customer-service:8085" },
          { name = "REPORT_SERVICE_URL", value = "http://report-service:8087" },
          { name = "AI_SERVICE_URL", value = "http://ai-service:5000" }
        ]
      )
    }

    # 2. FRONTEND WEB
    frontend = {
      port      = 80
      cpu       = 256
      mem       = 512
      mount_efs = true
      env       = []
    }

    # 3. CATALOG SERVICE
    catalog-service = {
      port      = 8083
      cpu       = 256
      mem       = 512
      mount_efs = true
      env = concat(
        local.common_db_env,
        [
          { name = "INVENTORY_SERVICE_URL", value = "http://inventory-service:8084" },
          { name = "BIZFLOW_ASSETS_DIR", value = "/assets" }
        ]
      )
    }

    # 4. SALES SERVICE
    sales-service = {
      port      = 8081
      cpu       = 512
      mem       = 1024
      mount_efs = false
      env = concat(
        local.common_db_env,
        local.common_mq_env,
        [
          { name = "CATALOG_SERVICE_URL", value = "http://catalog-service:8083" },
          { name = "INVENTORY_SERVICE_URL", value = "http://inventory-service:8084" },
          { name = "CUSTOMER_SERVICE_URL", value = "http://customer-service:8085" },
          { name = "AUTH_SERVICE_URL", value = "http://authentication-service:8086" },
          { name = "PROMOTION_SERVICE_URL", value = "http://promotion-service:8082" }
        ]
      )
    }

    # 5. INVENTORY SERVICE
    inventory-service = {
      port      = 8084
      cpu       = 256
      mem       = 512
      mount_efs = false
      env = concat(
        local.common_db_env,
        [
          { name = "CATALOG_SERVICE_URL", value = "http://catalog-service:8083" }
        ]
      )
    }

    # 6. CUSTOMER SERVICE
    customer-service = {
      port      = 8085
      cpu       = 256
      mem       = 512
      mount_efs = false
      env = concat(
        local.common_db_env,
        local.common_mq_env,
        [
          { name = "SALES_SERVICE_URL", value = "http://sales-service:8081" }
        ]
      )
    }

    # 7. AUTHENTICATION SERVICE
    authentication-service = {
      port      = 8086
      cpu       = 256
      mem       = 512
      mount_efs = false
      env = concat(
        local.common_db_env,
        local.common_mq_env,
        [
          { name = "JWT_SECRET", value = var.jwt_secret }
        ]
      )
    }

    # 8. PROMOTION SERVICE
    promotion-service = {
      port      = 8082
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = local.common_db_env
    }

    # 9. REPORT SERVICE
    report-service = {
      port      = 8087
      cpu       = 256
      mem       = 512
      mount_efs = false
      env = concat(
        local.common_db_env,
        [
          { name = "SALES_SERVICE_URL", value = "http://sales-service:8081" },
          { name = "CATALOG_SERVICE_URL", value = "http://catalog-service:8083" },
          { name = "INVENTORY_SERVICE_URL", value = "http://inventory-service:8084" },
          { name = "CUSTOMER_SERVICE_URL", value = "http://customer-service:8085" },
          { name = "AUTH_SERVICE_URL", value = "http://authentication-service:8086" }
        ]
      )
    }

    # 10. AI SERVICE
    ai-service = {
      port      = 5000
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = []
    }

    # 11. ADMIN USER SERVICE
    admin-user-service = {
      port      = 8201
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = local.common_db_env
    }

    # 12. ADMIN PRODUCT SERVICE
    admin-product-service = {
      port      = 8204
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = local.common_db_env
    }

    # 13. ADMIN ORDER SERVICE
    admin-order-service = {
      port      = 8203
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = local.common_db_env
    }

    # 14. ADMIN REPORT SERVICE
    admin-report-service = {
      port      = 8205
      cpu       = 256
      mem       = 512
      mount_efs = false
      env       = local.common_db_env # Also requires Kafka config if MSK is integrated
    }
  }
}

# ECS Task Definition for each service
resource "aws_ecs_task_definition" "service" {
  for_each                 = local.services
  family                   = "${var.project_name}-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = each.value.cpu
  memory                   = each.value.mem
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/bizflow/${each.key}:latest"
      essential = true
      portMappings = [
        {
          containerPort = each.value.port
          hostPort      = each.value.port
        }
      ]
      environment = each.value.env
      mountPoints = each.value.mount_efs ? [
        {
          sourceVolume  = "assets-volume"
          containerPath = each.key == "frontend" ? "/usr/share/nginx/html/assets" : "/assets"
          readOnly      = false
        }
      ] : []
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = each.key
        }
      }
    }
  ])

  dynamic "volume" {
    for_each = each.value.mount_efs ? [1] : []
    content {
      name = "assets-volume"
      efs_volume_configuration {
        file_system_id = aws_efs_file_system.shared_assets.id
        root_directory = "/"
      }
    }
  }
}

# ECS Service for each service
resource "aws_ecs_service" "service" {
  for_each        = local.services
  name            = each.key
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.service[each.key].arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  service_connect_configuration {
    enabled   = true
    namespace = aws_service_discovery_http_namespace.main.arn
    service {
      port_name     = each.key
      discovery_name = each.key
      client_alias {
        port = each.value.port
      }
    }
  }

  # Dynamically associate with target group if frontend or gateway
  dynamic "load_balancer" {
    for_each = each.key == "frontend" ? [1] : []
    content {
      target_group_arn = aws_lb_target_group.frontend.arn
      container_name   = "frontend"
      container_port   = 80
    }
  }

  dynamic "load_balancer" {
    for_each = each.key == "gateway" ? [1] : []
    content {
      target_group_arn = aws_lb_target_group.gateway.arn
      container_name   = "gateway"
      container_port   = 8000
    }
  }

  depends_on = [
    aws_lb_listener.http
  ]
}
