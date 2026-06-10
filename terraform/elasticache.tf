# Subnet group for Redis Cache
resource "aws_elasticache_subnet_group" "redis" {
  name       = "${var.project_name}-redis-subnet-group"
  subnet_ids = aws_subnet.database[*].id
}

# Redis Replication Group (Single-node configuration for dev/test cost efficiency)
resource "aws_elasticache_replication_group" "redis" {
  replication_group_id        = "${var.project_name}-redis"
  description                 = "Redis cluster for BizFlow caching"
  node_type                   = "cache.t4g.micro" # Smallest size for cost efficiency
  num_cache_clusters          = 1
  parameter_group_name        = "default.redis7"
  port                        = 6379
  subnet_group_name           = aws_elasticache_subnet_group.redis.name
  security_group_ids          = [aws_security_group.redis.id]
  automatic_failover_enabled  = false
  multi_az_enabled            = false

  tags = {
    Name = "${var.project_name}-redis-cache"
  }
}
