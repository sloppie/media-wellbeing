protoc --proto_path=proto-buffers --js_out=import_style=commonjs,binary:server/proto-buffers modelio.proto
protoc --proto_path=proto-buffers --python_out=. modelio.proto