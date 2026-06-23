CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    login VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    displayName VARCHAR(255),
    avatar TEXT,
    isAdmin BOOLEAN DEFAULT false
);

INSERT INTO users (id, login, password, displayName, avatar, isAdmin)
VALUES (
    '0fe4f25c-eadd-4523-9a8c-8f36932c5732', 
    'admin.admin', 
    '4ZNFLYQvubHcoNV/84BktWluzvMxp8kqbheuyUxcRbRq', 
    'Admin Admin', 
    '', 
    true
);