# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: modelio.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='modelio.proto',
  package='ecd',
  syntax='proto2',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\rmodelio.proto\x12\x03\x65\x63\x64\"H\n\tModelArgs\x12\x12\n\nmedia_type\x18\x01 \x02(\x05\x12\x11\n\tfile_name\x18\x02 \x02(\t\x12\x14\n\x0crequest_type\x18\x03 \x02(\x05\"M\n\rModelResponse\x12\x13\n\x0bis_explicit\x18\x01 \x02(\x05\x12\x11\n\tfile_name\x18\x02 \x02(\t\x12\x14\n\x0crequest_type\x18\x03 \x02(\x05'
)




_MODELARGS = _descriptor.Descriptor(
  name='ModelArgs',
  full_name='ecd.ModelArgs',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='media_type', full_name='ecd.ModelArgs.media_type', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='ecd.ModelArgs.file_name', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='request_type', full_name='ecd.ModelArgs.request_type', index=2,
      number=3, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=22,
  serialized_end=94,
)


_MODELRESPONSE = _descriptor.Descriptor(
  name='ModelResponse',
  full_name='ecd.ModelResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='is_explicit', full_name='ecd.ModelResponse.is_explicit', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='file_name', full_name='ecd.ModelResponse.file_name', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='request_type', full_name='ecd.ModelResponse.request_type', index=2,
      number=3, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=96,
  serialized_end=173,
)

DESCRIPTOR.message_types_by_name['ModelArgs'] = _MODELARGS
DESCRIPTOR.message_types_by_name['ModelResponse'] = _MODELRESPONSE
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ModelArgs = _reflection.GeneratedProtocolMessageType('ModelArgs', (_message.Message,), {
  'DESCRIPTOR' : _MODELARGS,
  '__module__' : 'modelio_pb2'
  # @@protoc_insertion_point(class_scope:ecd.ModelArgs)
  })
_sym_db.RegisterMessage(ModelArgs)

ModelResponse = _reflection.GeneratedProtocolMessageType('ModelResponse', (_message.Message,), {
  'DESCRIPTOR' : _MODELRESPONSE,
  '__module__' : 'modelio_pb2'
  # @@protoc_insertion_point(class_scope:ecd.ModelResponse)
  })
_sym_db.RegisterMessage(ModelResponse)


# @@protoc_insertion_point(module_scope)
