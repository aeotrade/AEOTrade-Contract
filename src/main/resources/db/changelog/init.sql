--liquibase formatted sql
--changeset chl:init
drop index if exists contract_PK;

drop table if exists contract;

drop index if exists contract_org_alias_PK;

drop table if exists contract_org_alias;

drop index if exists contract_org_config_PK;

drop table if exists contract_org_config;

drop index if exists contract_org_release_PK;

drop table if exists contract_org_release;

drop index if exists idx_cr_contract_instance;

drop index if exists contract_record_PK;

drop table if exists contract_record;

drop index if exists contract_template_PK;

drop table if exists contract_template;

drop index if exists contract_template_kind_PK;

drop table if exists contract_template_kind;

drop index if exists contract_template_kind_rel_PK;

drop table if exists contract_template_kind_rel;

drop index if exists contract_template_org_alias_PK;

drop table if exists contract_template_org_alias;

drop index if exists dxp_msg_receive_PK;

drop table if exists dxp_msg_receive;

drop index if exists dxp_msg_send_PK;

drop table if exists dxp_msg_send;

/*==============================================================*/
/* Table: contract                                              */
/*==============================================================*/
create table contract (
                          contract_id          INT8                 not null,
                          customer_contract_id VARCHAR(128)         null,
                          organization_id      VARCHAR(64)          null,
                          icon                 VARCHAR(1024)        null,
                          contract_template_id INT8                 null,
                          name                 VARCHAR(128)         null,
                          decription           VARCHAR(1024)        null,
                          type                 VARCHAR(4)           null,
                          process_defintion    TEXT                 null,
                          process_definition_key VARCHAR(64)          null,
                          process_definition_id VARCHAR(64)          null,
                          version              VARCHAR(32)          null,
                          create_date          TIMESTAMP WITH TIME ZONE null,
                          create_uid           VARCHAR(64)          null,
                          write_time           TIMESTAMP WITH TIME ZONE null,
                          write_uid            VARCHAR(64)          null,
                          release_status       VARCHAR(4)           null,
                          execute_status       VARCHAR(4)           null,
                          remaining_num        INT4                 null,
                          done_qty             INT4                 null,
                          target_qty           INT4                 null,
                          task_error_msg       VARCHAR(2048)        null,
                          constraint PK_CONTRACT primary key (contract_id),
                          constraint AK_CUSTOM_CONTRACT_ID_CONTRACT unique (customer_contract_id)
);


/*==============================================================*/
/* Index: contract_PK                                           */
/*==============================================================*/
create unique index contract_PK on contract (
                                             contract_id
    );

/*==============================================================*/
/* Table: contract_org_alias                                    */
/*==============================================================*/
create table contract_org_alias (
                                    alias_seq            INT8                 not null,
                                    alias_id             INT8                 null,
                                    contract_id          INT8                 not null,
                                    alias_name           VARCHAR(128)         null,
                                    collaboration_org_id VARCHAR(32)          null,
                                    constraint PK_CONTRACT_ORG_ALIAS primary key (alias_seq)
);

/*==============================================================*/
/* Index: contract_org_alias_PK                                 */
/*==============================================================*/
create unique index contract_org_alias_PK on contract_org_alias (
                                                                 alias_seq
    );

/*==============================================================*/
/* Table: contract_org_config                                   */
/*==============================================================*/
create table contract_org_config (
                                     contract_org_config_seq INT8                 not null,
                                     contract_id          INT8                 not null,
                                     collaboration_org_id VARCHAR(32)          not null,
                                     hidden               BOOL                 null,
                                     hidden_op_time       TIMESTAMP WITH TIME ZONE null,
                                     constraint PK_CONTRACT_ORG_CONFIG primary key (contract_org_config_seq),
                                     constraint AK_COC_CONTRACT_ORG_CONTRACT unique (contract_id, collaboration_org_id)
);

/*==============================================================*/
/* Index: contract_org_config_PK                                */
/*==============================================================*/
create unique index contract_org_config_PK on contract_org_config (
                                                                   contract_org_config_seq
    );

/*==============================================================*/
/* Table: contract_org_release                                  */
/*==============================================================*/
create table contract_org_release (
                                      contract_org_release_seq INT8                 not null,
                                      contract_id          INT8                 not null,
                                      collaboration_org_id VARCHAR(32)          null,
                                      release_status       VARCHAR(4)           null,
                                      release_time         TIMESTAMP WITH TIME ZONE null,
                                      constraint PK_CONTRACT_ORG_RELEASE primary key (contract_org_release_seq)
);

/*==============================================================*/
/* Index: contract_org_release_PK                               */
/*==============================================================*/
create unique index contract_org_release_PK on contract_org_release (
                                                                     contract_org_release_seq
    );

/*==============================================================*/
/* Table: contract_record                                       */
/*==============================================================*/
create table contract_record (
                                 contract_record_id   INT8                 not null,
                                 contract_id          INT8                 not null,
                                 name                 VARCHAR(128)         null,
                                 organization_id      VARCHAR(64)          null,
                                 org_contract_record_no VARCHAR(128)         null,
                                 process_instance_id  VARCHAR(64)          null,
                                 process_definition_id VARCHAR(64)          null,
                                 create_date          TIMESTAMP WITH TIME ZONE null,
                                 constraint PK_CONTRACT_RECORD primary key (contract_record_id)
);

/*==============================================================*/
/* Index: contract_record_PK                                    */
/*==============================================================*/
create unique index contract_record_PK on contract_record (
                                                           contract_record_id
    );

/*==============================================================*/
/* Index: idx_cr_contract_instance                              */
/*==============================================================*/
create  index idx_cr_contract_instance on contract_record (
                                                           contract_id,
                                                           org_contract_record_no
    );

/*==============================================================*/
/* Table: contract_template                                     */
/*==============================================================*/
create table contract_template (
                                   contract_template_id INT8                 not null,
                                   custom_contract_template_id VARCHAR(128)         null,
                                   icon                 VARCHAR(1024)        null,
                                   name                 VARCHAR(128)         null,
                                   decription           VARCHAR(1024)        null,
                                   type                 VARCHAR(4)           null,
                                   process_defintion    TEXT                 null,
                                   version              VARCHAR(32)          null,
                                   recommend            CHAR(1)              null,
                                   solution_description TEXT                 null,
                                   create_date          TIMESTAMP WITH TIME ZONE null,
                                   create_uid           VARCHAR(64)          null,
                                   write_time           TIMESTAMP WITH TIME ZONE null,
                                   write_uid            VARCHAR(64)          null,
                                   constraint PK_CONTRACT_TEMPLATE primary key (contract_template_id),
                                   constraint AK_CUS_CON_TEMPLATE_I_CONTRACT unique (custom_contract_template_id)
);


/*==============================================================*/
/* Index: contract_template_PK                                  */
/*==============================================================*/
create unique index contract_template_PK on contract_template (
                                                               contract_template_id
    );

/*==============================================================*/
/* Table: contract_template_kind                                */
/*==============================================================*/
create table contract_template_kind (
                                        kind_id              INT8                 not null,
                                        sort                 INT4                 null,
                                        category_name        VARCHAR(64)          null,
                                        category_code        VARCHAR(32)          null,
                                        note                 VARCHAR(256)         null,
                                        create_date          TIMESTAMP WITH TIME ZONE null,
                                        create_uid           VARCHAR(64)          null,
                                        write_time           TIMESTAMP WITH TIME ZONE null,
                                        write_uid            VARCHAR(64)          null,
                                        description          VARCHAR(512)         null,
                                        constraint PK_CONTRACT_TEMPLATE_KIND primary key (kind_id),
                                        constraint AK_UK_CTK_CODE_CONTRACT unique (category_code)
);

/*==============================================================*/
/* Index: contract_template_kind_PK                             */
/*==============================================================*/
create unique index contract_template_kind_PK on contract_template_kind (
                                                                         kind_id
    );

/*==============================================================*/
/* Table: contract_template_kind_rel                            */
/*==============================================================*/
create table contract_template_kind_rel (
                                            rel_seq              INT8                 not null,
                                            contract_template_id INT8                 not null,
                                            kind_id              INT8                 not null,
                                            create_date          TIMESTAMP WITH TIME ZONE null,
                                            create_uid           VARCHAR(64)          null,
                                            constraint PK_CONTRACT_TEMPLATE_KIND_REL primary key (rel_seq),
                                            constraint AK_UK_CTKR_ID_CODE_CONTRACT unique (contract_template_id, kind_id)
);

/*==============================================================*/
/* Index: contract_template_kind_rel_PK                         */
/*==============================================================*/
create unique index contract_template_kind_rel_PK on contract_template_kind_rel (
                                                                                 rel_seq
    );

/*==============================================================*/
/* Table: contract_template_org_alias                           */
/*==============================================================*/
create table contract_template_org_alias (
                                             alias_seq            INT8                 not null,
                                             alias_id             INT8                 null,
                                             contract_template_id INT8                 not null,
                                             alias_name           VARCHAR(128)         null,
                                             constraint PK_CONTRACT_TEMPLATE_ORG_ALIAS primary key (alias_seq)
);

/*==============================================================*/
/* Index: contract_template_org_alias_PK                        */
/*==============================================================*/
create unique index contract_template_org_alias_PK on contract_template_org_alias (
                                                                                   alias_seq
    );