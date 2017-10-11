CREATE TABLE fwArchiveConversations (
  conversationId        BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT          NOT NULL,
  ownerJid              VARCHAR(255)    NOT NULL,
  ownerResource         VARCHAR(255),
  withJid               VARCHAR(255)    NOT NULL,
  withResource          VARCHAR(255),
  subject               VARCHAR(255),
  thread                VARCHAR(255),
  PRIMARY KEY (conversationId),
  -- 建立索引
  INDEX idx_fwArchiveConversations_startTime (startTime),
  INDEX idx_fwArchiveConversations_endTime (endTime),
  INDEX idx_fwArchiveConversations_ownerJid (ownerJid),
  INDEX idx_fwArchiveConversations_withJid (withJid)
);
-- 好像也没用到
CREATE TABLE fwArchiveParticipants (
  participantId         BIGINT          NOT NULL,
  startTime             BIGINT          NOT NULL,
  endTime               BIGINT,
  jid                   VARCHAR(255)    NOT NULL,
  nick                  VARCHAR(255),
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (participantId),
  INDEX idx_fwArchiveParticipants_conversationId (conversationId),
  INDEX idx_fwArchiveParticipants_jid (jid)
);

CREATE TABLE fwArchiveMessages (
  messageId             BIGINT          NOT NULL,
  time                  BIGINT          NOT NULL,
  direction             CHAR(4)         NOT NULL,
  type                  CHAR(15)        NOT NULL,
  subject               VARCHAR(255),
  body                  TEXT,
  conversationId        BIGINT          NOT NULL,
  PRIMARY KEY (messageId),
  INDEX idx_fwArchiveMessages_conversationId (conversationId),
  INDEX idx_fwArchiveMessages_time (time)
);

-- 这个表没用到，预设置内容
CREATE TABLE fwArchivePrefItems (
  username              VARCHAR(64)     NOT NULL,
  jid                   VARCHAR(255),
  saveMode              INTEGER,
  otrMode               INTEGER,
  expireTime            BIGINT,
  PRIMARY KEY (username,jid)
);

-- 这个表没用到，预设置内容
CREATE TABLE fwArchivePrefMethods (
  username              VARCHAR(64)     NOT NULL,
  methodType            VARCHAR(255)    NOT NULL,
  methodUsage           INTEGER,
  PRIMARY KEY (username,methodType)
);

INSERT INTO ofVersion (name, version) VALUES ('fwArchive', 1);
