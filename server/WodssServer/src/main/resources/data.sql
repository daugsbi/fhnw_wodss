insert into login_data (password, salt, validated, validation_code) values (X'75d8ee9b3c1785d0d8529a90b5892d22cc829de55896067d6d54681ce6b8cb2259274de9ae3ad833b61c591106efdcfae6dcb42df21cbc77eb56535eec16141b', X'3d89f71bc766c9cd95fa4e728197360fd8bf5a5ae47b1c9042b1ea410a928bd6', true, '9ae3499d-63e5-40bb-9be6-9f868c8946a5');
insert into user (name, email, login_data_id) values ('Hans Muster', 'hans.muster@fhnw.ch', 1);