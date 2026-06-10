# S3 Bucket for static frontend files
resource "aws_s3_bucket" "frontend_static" {
  bucket        = "${var.project_name}-frontend-static-bucket-${random_string.suffix.result}"
  force_destroy = true
}

# Generate random string for unique bucket name
resource "random_string" "suffix" {
  length  = 6
  special = false
  upper   = false
}

# Public access block
resource "aws_s3_bucket_public_access_block" "public_block" {
  bucket = aws_s3_bucket.frontend_static.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# Bucket policy to allow read access from anywhere
resource "aws_s3_bucket_policy" "read_policy" {
  bucket     = aws_s3_bucket.frontend_static.id
  depends_on = [aws_s3_bucket_public_access_block.public_block]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObject"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource  = "${aws_s3_bucket.frontend_static.arn}/*"
      }
    ]
  })
}

# Enable static website hosting
resource "aws_s3_bucket_website_configuration" "static_hosting" {
  bucket = aws_s3_bucket.frontend_static.id

  index_document {
    suffix = "pages/login.html"
  }
}
