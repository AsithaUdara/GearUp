-- Create parts service database and user
CREATE DATABASE as_parts_service;
CREATE USER svc_parts_service WITH PASSWORD 'parts_svc_pass_2024';
GRANT ALL PRIVILEGES ON DATABASE as_parts_service TO svc_parts_service;