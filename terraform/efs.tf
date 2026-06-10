# Security Group for EFS mount targets
resource "aws_security_group" "efs" {
  name        = "${var.project_name}-efs-sg"
  description = "Security group for EFS mount targets"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Allow NFS access from ECS tasks"
    from_port       = 2049
    to_port         = 2049
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-efs-sg"
  }
}

# EFS File System for shared product assets
resource "aws_efs_file_system" "shared_assets" {
  creation_token = "${var.project_name}-assets-efs"
  encrypted      = true

  tags = {
    Name = "${var.project_name}-assets-efs"
  }
}

# EFS Mount Targets in Private Subnets (where ECS tasks run)
resource "aws_efs_mount_target" "assets" {
  count           = 2
  file_system_id  = aws_efs_file_system.shared_assets.id
  subnet_id       = aws_subnet.private[count.index].id
  security_groups = [aws_security_group.efs.id]
}
