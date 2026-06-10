# Subnet group for RDS DB
resource "aws_db_subnet_group" "db" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = aws_subnet.database[*].id

  tags = {
    Name = "${var.project_name}-db-subnet-group"
  }
}

# RDS MySQL Instance
resource "aws_db_instance" "mysql" {
  identifier             = "${var.project_name}-mysql"
  engine                 = "mysql"
  engine_version         = "8.0"
  instance_class         = "db.t3.micro" # Smallest size for cost efficiency
  allocated_storage      = 20            # 20 GB gp2
  max_allocated_storage  = 100           # Auto-scaling storage up to 100 GB
  db_name                = "bizflow_catalog_db" # Init default database
  username               = "root"
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.db.name
  vpc_security_group_ids = [aws_security_group.db.id]
  skip_final_snapshot    = true
  publicly_accessible    = false

  tags = {
    Name = "${var.project_name}-mysql-db"
  }
}
