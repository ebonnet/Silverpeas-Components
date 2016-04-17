CREATE TABLE sc_quickinfo_news (
  id             		VARCHAR(40) PRIMARY KEY,
  instanceId     		VARCHAR(30) NOT NULL,
  foreignId		 		VARCHAR(40) NOT NULL,
  important      		BOOLEAN	 	NOT NULL,
  broadcastTicker		BOOLEAN 	NOT NULL,
  broadcastMandatory	BOOLEAN		NOT NULL,
  createDate     		TIMESTAMP   NOT NULL,
  createdBy      		VARCHAR(40) NOT NULL,
  lastUpdateDate 		TIMESTAMP   NOT NULL,
  lastUpdatedBy  		VARCHAR(40) NOT NULL,
  publishDate 			TIMESTAMP   NULL,
  publishedBy  			VARCHAR(40) NULL,
  version        		INT8        NOT NULL
);

INSERT INTO sc_quickinfo_news (id, instanceId, foreignId,
	important, broadcastTicker, broadcastMandatory,
	createDate, createdBy, lastUpdateDate, lastUpdatedBy,
	publishDate, publishedBy, version)
  SELECT
    cast(pubId AS TEXT),
    instanceId,
	cast(pubId AS TEXT),
    false,
    false,
    false,
    to_timestamp(pubCreationDate, 'YYYY/MM/DD'),
	pubCreatorId,
	to_timestamp(pubUpdateDate, 'YYYY/MM/DD'),
    pubUpdaterId,
	to_timestamp(pubCreationDate, 'YYYY/MM/DD'),
	pubCreatorId,
    0
  FROM
    sb_publication_publi
  WHERE
	instanceId like 'quickinfo%';