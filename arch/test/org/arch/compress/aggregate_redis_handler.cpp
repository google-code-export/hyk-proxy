/* samzi 20110726 */

#include <pthread.h>
#include <time.h>
#include "aggregate_redis_handler.hpp"
#include "common/redis_type.hpp"
#include "concurrent/signal_handler.hpp"
#include "util/redis_connection.hpp"
#include "esa/esa_debug_stat_worker.hpp"

using namespace std;
using namespace arch::common;
using namespace arch::concurrent;
using namespace rta::util;
using namespace rta::common;
using namespace wbl::util;
using namespace wbl::log;

bool g_cmd_init_flag = false;
string g_str_hincrby;
string g_str_hset;
string g_str_hmset;
string g_str_hget;
string g_str_hmget;
string g_str_sadd;
string g_str_key_dm;
string g_str_smembers;

string g_str_vip_pv;
string g_str_vip_uv;
string g_str_vip_vv;
string g_str_vip_iv;
string g_str_vip_thread;
string g_str_vip_post;
string g_str_vip_time;
string g_str_vip_bounce;
string g_str_vip_land;
string g_str_vip_exp;

string g_str_guest_pv;
string g_str_guest_uv;
string g_str_guest_vv;
string g_str_guest_iv;
string g_str_guest_thread;
string g_str_guest_post;
string g_str_guest_time;
string g_str_guest_bounce;
string g_str_guest_land;
string g_str_guest_exp;

string g_str_all_pv;
string g_str_all_uv;
string g_str_all_vv;
string g_str_all_iv;
string g_str_all_thread;
string g_str_all_post;
string g_str_all_time;
string g_str_all_bounce;
string g_str_all_land;
string g_str_all_exp;

string g_str_sum_pv;
string g_str_sum_uv;
string g_str_sum_vv;
string g_str_sum_iv;
string g_str_sum_thread;
string g_str_sum_post;
string g_str_sum_time;
string g_str_sum_bounce;

string g_str_vip_cnt;
string g_str_old_vip_cnt;
string g_str_guest_cnt;
string g_str_new_guest_cnt;

void init_redis_cmds()
{
    if (true == g_cmd_init_flag)
    {
        return;
    }
    g_cmd_init_flag = true;

    char buf[ESA_DEFAULT_BUF_LEN_];
    g_str_hincrby = "hincrby";
    g_str_hset = "hset";
    g_str_hmset = "hmset";
    g_str_hget = "hget";
    g_str_hmget = "hmget";
    g_str_sadd = "sadd";
    g_str_smembers = "smembers";

    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "0:%u", RTA_REDIS_DM);
    g_str_key_dm = buf;

    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_PV, USER_TYPE_VIP);
    g_str_vip_pv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_UV, USER_TYPE_VIP);
    g_str_vip_uv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_VV, USER_TYPE_VIP);
    g_str_vip_vv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_IV, USER_TYPE_VIP);
    g_str_vip_iv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_THREAD_CNT,
            USER_TYPE_VIP);
    g_str_vip_thread = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_POST_CNT,
            USER_TYPE_VIP);
    g_str_vip_post = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_TIME, USER_TYPE_VIP);
    g_str_vip_time = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_BOUNCE,
            USER_TYPE_VIP);
    g_str_vip_bounce = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_LAND, USER_TYPE_VIP);
    g_str_vip_land = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_EXP, USER_TYPE_VIP);
    g_str_vip_exp = buf;

    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_PV, USER_TYPE_GUEST);
    g_str_guest_pv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_UV, USER_TYPE_GUEST);
    g_str_guest_uv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_VV, USER_TYPE_GUEST);
    g_str_guest_vv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_IV, USER_TYPE_GUEST);
    g_str_guest_iv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_THREAD_CNT,
            USER_TYPE_GUEST);
    g_str_guest_thread = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_POST_CNT,
            USER_TYPE_GUEST);
    g_str_guest_post = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_TIME,
            USER_TYPE_GUEST);
    g_str_guest_time = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_BOUNCE,
            USER_TYPE_GUEST);
    g_str_guest_bounce = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_LAND,
            USER_TYPE_GUEST);
    g_str_guest_land = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_EXP,
            USER_TYPE_GUEST);
    g_str_guest_exp = buf;

    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_PV, USER_TYPE_ALL);
    g_str_all_pv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_UV, USER_TYPE_ALL);
    g_str_all_uv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_VV, USER_TYPE_ALL);
    g_str_all_vv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_IV, USER_TYPE_ALL);
    g_str_all_iv = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_THREAD_CNT,
            USER_TYPE_ALL);
    g_str_all_thread = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_POST_CNT,
            USER_TYPE_ALL);
    g_str_all_post = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_TIME, USER_TYPE_ALL);
    g_str_all_time = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_BOUNCE,
            USER_TYPE_ALL);
    g_str_all_bounce = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_LAND, USER_TYPE_ALL);
    g_str_all_land = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u:%u", RTA_REDIS_EXP, USER_TYPE_ALL);
    g_str_all_exp = buf;

    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u", RTA_REDIS_VIP);
    g_str_vip_cnt = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u", RTA_REDIS_OLD_VIP);
    g_str_old_vip_cnt = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u", RTA_REDIS_GUEST);
    g_str_guest_cnt = buf;
    snprintf(buf, ESA_DEFAULT_BUF_LEN_, "%u", RTA_REDIS_NEW_GUEST);
    g_str_new_guest_cnt = buf;
}

void print_vector(uint32 index, vector<string> &vec )
{
    int n = vec.size();

    INFO_LOG("## ");
    for (int i = 0; i < n; i++)
    {
        INFO_LOG("[%u]%s ", index, vec[i].c_str());
    }
}

void AggregateRedisSelectDBCallBack(RedisConnection* conn, redisReply* reply,
        void* data)
{
    AggregateRedisHandler* p_handler = (AggregateRedisHandler*) data;

    if (reply->type == REDIS_REPLY_STATUS && reply->len > 0
            && 0 == strcmp("OK", reply->str))
    {
        p_handler->SetSelectDBStatus(true);
        INFO_LOG("select DB OK");
        p_handler->CheckIfNeedToLoadFromRedis();
    }
    else
    {
        p_handler->SetSelectDBStatus(false);
        DEBUG_LOG("select DB ERR");
    }
}

void AggregateRedisWriteCallBack(RedisConnection* conn, redisReply* reply,
        void* data)
{
    ReplyUn* p_reply = (ReplyUn*) data;
    AggregateRedisHandler* p_handler =
            (AggregateRedisHandler*) (p_reply->w_reply.ptr);

    if (REDIS_REPLY_INTEGER == reply->type)
    //&& reply->integer >= 0 )
    {
        p_handler->AddReplyCount(1);
    }
    else if (REDIS_REPLY_STATUS == reply->type && reply->len > 0
            && 0 == strcmp("OK", reply->str))
    {
        p_handler->AddReplyCount(1);
    }
    else if (REDIS_REPLY_STRING == reply->type && reply->len > 0)
    {
        p_handler->AddReplyCount(1);
    }
    else
    {
        SessionSt* p_session_st = NULL;
        p_session_st = p_handler->GetSessionManager().GetSessionByPos(
                p_reply->w_reply.session_pos, p_reply->w_reply.session_id);
        if (NULL == p_session_st)
        {
            p_handler->AddReplyCount(1);
            return;
        }

        //print_vector( p_session_st->cmd );
        DEBUG_LOG("------");
        DEBUG_LOG("type:%d", reply->type);
        DEBUG_LOG("integer:%lld", reply->integer);
        DEBUG_LOG("len:%d", reply->len);
        DEBUG_LOG("str:%s", ( (NULL == reply->str)? "NULL": reply->str ));
        DEBUG_LOG("elements:%d", reply->elements);

        if (p_reply->w_reply.retry_count < 2)
        {
            DEBUG_LOG("retry");
            p_reply->w_reply.retry_count++;
            p_handler->GetRedisConn()->AsyncCommand(AggregateRedisWriteCallBack,
                    p_reply, p_session_st->cmd);
        }
        else
        {
            //-- try to log these data into file 
            p_handler->Log2File(p_session_st->cmd);
            p_handler->AddReplyCount(1);
        }
    }
    if (p_handler->m_reply_count >= p_handler->m_send_count)
    {
        //-- all replies are already received
        //-- cancel timer
        p_handler->ResetTimer();
        if (false == p_handler->m_dump_fin)
        {
            //-- dumping is not finished, we have to continue 
            p_handler->RunCmd();
        }
    }
}

void AggregateRedisReadDMCallBack(RedisConnection* conn, redisReply* reply,
        void* data)
{
    ReplyUn* p_reply = (ReplyUn*) data;
    AggregateRedisHandler* p_handler =
            (AggregateRedisHandler*) (p_reply->r_reply.ptr);

    if (REDIS_REPLY_ARRAY != reply->type)
    {
        // error happened!
        ERROR_LOG(
                "load domain from redis error, redis reply type:%d, len:%d, str:%s ", reply->type, reply->len, reply->str);
        return;
    }

    if (reply->elements >= 0)
    {
        redisReply* tmp_reply = NULL;
        DEBUG_LOG("== the elements == ");
        for (int i = 0; i < reply->elements; i++)
        {
            tmp_reply = reply->element[i];
            /*
             DEBUG_LOG("--");
             DEBUG_LOG("type:%d", tmp_reply->type);
             DEBUG_LOG("integer:%lld", tmp_reply->integer);
             DEBUG_LOG("len:%d", tmp_reply->len);
             */
            if (tmp_reply->len > 0)
            {
                DEBUG_LOG("str:%s", tmp_reply->str);
                p_handler->AddDomainToVec(tmp_reply->str);
            }
            DEBUG_LOG("elements:%d", tmp_reply->elements);
        }
        p_handler->ResetTimer();
        p_handler->SetRedisLoadStatus(ESA_REDIS_LOADING_DOMAINS_OK_);
        p_handler->AddReplyCount(1);
        p_handler->LoadFromRedis();
    }
}

void AggregateRedisReadDMDetailCallback(RedisConnection* conn,
        redisReply* reply, void* data)
{
    ReplyUn* p_reply = (ReplyUn*) data;
    AggregateRedisHandler* p_handler =
            (AggregateRedisHandler*) (p_reply->r_reply.ptr);

    DEBUG_LOG(
            "type:%u, isvip:%u, domain:%u, ext2:%llu ", p_reply->r_reply.type, p_reply->r_reply.flag, p_reply->r_reply.ext1, p_reply->r_reply.ext2);

    if (reply->elements != 8)
    {
        ERROR_LOG("load domain detail from redis, reply elements less than 8");
        SessionSt* p_session_st = NULL;
        p_session_st = p_handler->GetSessionManager().GetSessionByPos(
                p_reply->r_reply.session_pos, p_reply->r_reply.session_id);
        if (NULL == p_session_st)
        {
            return;
        }
        if (p_reply->r_reply.retry_count < 2)
        {
            DEBUG_LOG("retry");
            p_handler->GetRedisConn()->AsyncCommand(
                    AggregateRedisReadDMDetailCallback, p_reply,
                    p_session_st->cmd);
            p_reply->r_reply.retry_count++;
            return;
        }
        else
        {
            //-- error happen
            return;
        }
    }
    else
    {
        ESADomainItem dm_item;
        bool b_has_data = false;
        memset(&dm_item, 0x00, sizeof(ESADomainItem));
        dm_item.isvip = p_reply->r_reply.flag;
        redisReply* tmp_reply = NULL;
        tmp_reply = reply->element[0];
        if (tmp_reply->len > 0)
        {
            dm_item.pv = atoi(tmp_reply->str);
            b_has_data = true;
            //TRACE_LOG("pv:%u", dm_item.pv);
        }
        tmp_reply = reply->element[1];
        if (tmp_reply->len > 0)
        {
            dm_item.uv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("uv:%u", dm_item.uv);
        }
        tmp_reply = reply->element[2];
        if (tmp_reply->len > 0)
        {
            dm_item.vv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("vv:%u", dm_item.vv);
        }
        tmp_reply = reply->element[3];
        if (tmp_reply->len > 0)
        {
            dm_item.iv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("iv:%u", dm_item.iv);
        }
        tmp_reply = reply->element[4];
        if (tmp_reply->len > 0)
        {
            dm_item.thread = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("thread:%u", dm_item.thread);
        }
        tmp_reply = reply->element[5];
        if (tmp_reply->len > 0)
        {
            dm_item.post = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("post:%u", dm_item.post);
        }
        tmp_reply = reply->element[6];
        if (tmp_reply->len > 0)
        {
            dm_item.time = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("time:%u", dm_item.time);
        }
        tmp_reply = reply->element[7];
        if (tmp_reply->len > 0)
        {
            dm_item.bounce = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //TRACE_LOGINFO_LOG("bounce:%u", dm_item.bounce);
        }
        //p_handler->m_reply_count++;
        p_handler->AddReplyCount(1);
        if (true == b_has_data)
        {
            p_handler->InsertSumItemToHeap(rta::common::RTA_REDIS_DM,
                    p_reply->r_reply.ext1, (void*) &dm_item);
        }
    }
    p_handler->LoadFromRedis();
}

void AggregateRedisReadDMRefUrlCallBack(RedisConnection* conn,
        redisReply* reply, void* data)
{
    ReplyUn* p_reply = (ReplyUn*) data;
    AggregateRedisHandler* p_handler =
            (AggregateRedisHandler*) (p_reply->r_reply.ptr);
    if (REDIS_REPLY_ARRAY != reply->type)
    {
        // error happened!
        ERROR_LOG(
                "load domain refer from redis error, redis reply type:%d, len:%d, str:%s ", reply->type, reply->len, reply->str);
        return;
    }

    if (reply->elements >= 0)
    {
        redisReply* tmp_reply = NULL;
        DEBUG_LOG("== the elements == ");
        for (int i = 0; i < reply->elements; i++)
        {
            tmp_reply = reply->element[i];
            /*
             DEBUG_LOG("--");
             DEBUG_LOG("type:%d", tmp_reply->type);
             DEBUG_LOG("integer:%lld", tmp_reply->integer);
             DEBUG_LOG("len:%d", tmp_reply->len);
             */
            if (tmp_reply->len > 0)
            {
                //INFO_LOG("str:%s", tmp_reply->str);
                p_handler->AddTmpStrToVec(tmp_reply->str);
            }
            DEBUG_LOG("elements:%d", tmp_reply->elements);
        }
        p_handler->ResetTimer();
        p_handler->AddReplyCount(1);
        p_handler->LoadFromRedis();
    }
}

void AggregateRedisReadDMRefDataCallBack(RedisConnection* conn,
        redisReply* reply, void* data)
{
    ReplyUn* p_reply = (ReplyUn*) data;
    AggregateRedisHandler* p_handler =
            (AggregateRedisHandler*) (p_reply->r_reply.ptr);
    /*
     INFO_LOG("type:%u, isvip:%u, domain:%u, ext2:%llu ",
     p_reply->r_reply.type,
     p_reply->r_reply.flag,
     p_reply->r_reply.ext1,
     p_reply->r_reply.ext2);
     */

    if (reply->elements != 6)
    {
        ERROR_LOG("load domain detail from redis, reply elements less than 8");
        SessionSt* p_session_st = NULL;
        p_session_st = p_handler->GetSessionManager().GetSessionByPos(
                p_reply->r_reply.session_pos, p_reply->r_reply.session_id);
        if (NULL == p_session_st)
        {
            return;
        }
        if (p_reply->r_reply.retry_count < 2)
        {
            DEBUG_LOG("retry");
            p_handler->GetRedisConn()->AsyncCommand(
                    AggregateRedisReadDMRefDataCallBack, p_reply,
                    p_session_st->cmd);
            p_reply->r_reply.retry_count++;
            return;
        }
        else
        {
            //-- error happen
            return;
        }
    }
    else
    {
        struct ReferBasicEvent ref_item;
        bool b_has_data = false;

        memset(&ref_item, 0x00, sizeof(struct ReferBasicEvent));
        ref_item.domain = p_reply->r_reply.ext1;
        ref_item.refer = p_reply->r_reply.ext2;
        ref_item.isvip = p_reply->r_reply.flag;

        redisReply* tmp_reply = NULL;
        tmp_reply = reply->element[0];
        if (tmp_reply->len > 0)
        {
            ref_item.increasedpv = atoi(tmp_reply->str);
            b_has_data = true;
            //INFO_LOG("pv:%u", ref_item.increasedpv);
        }
        tmp_reply = reply->element[1];
        if (tmp_reply->len > 0)
        {
            ref_item.increaseduv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //INFO_LOG("uv:%u", ref_item.increaseduv);
        }
        tmp_reply = reply->element[2];
        if (tmp_reply->len > 0)
        {
            ref_item.increasedvv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //INFO_LOG("vv:%u", ref_item.increasedvv);
        }
        tmp_reply = reply->element[3];
        if (tmp_reply->len > 0)
        {
            ref_item.increasediv = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //INFO_LOG("iv:%u", ref_item.increasediv);
        }
        tmp_reply = reply->element[4];
        if (tmp_reply->len > 0)
        {
            ref_item.time = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //INFO_LOG("time:%u", ref_item.time);
        }
        tmp_reply = reply->element[5];
        if (tmp_reply->len > 0)
        {
            ref_item.bounce = atoi(tmp_reply->str);
            if (false == b_has_data)
            {
                b_has_data = true;
            }
            //INFO_LOG("bounce:%u", ref_item.bounce);
        }

        p_handler->AddReplyCount(1);
        if (true == b_has_data)
        {
            p_handler->InsertSumItemToHeap(rta::common::RTA_REDIS_DM_REF,
                    p_reply->r_reply.ext1, (void*) &ref_item);
        }
    }
    p_handler->LoadFromRedis();
}

AggregateRedisHandler::AggregateRedisHandler()
{
    m_p_logger = NULL;
}

AggregateRedisHandler::~AggregateRedisHandler()
{
    if (NULL != m_p_redis_conn)
    {
        delete (m_p_redis_conn);
    }
}

int AggregateRedisHandler::Init(RedisConnection *p_conn,
        ChannelService* p_channel_service, uint32 hash, uint32 hash_result,
        HeapManager* p_heap_manager, int index, wbl::log::dater *p_logger,
        int load_from_redis_flag)
{
    if (NULL == p_conn || NULL == p_channel_service || NULL == p_heap_manager)
    {
        return -1;
    }
    m_ready_to_set_date = false;
    m_ready_to_set_hour = false;
    m_wait_cnt = 0;
    m_hash = hash;
    m_hash_result = hash_result;
    m_reply_count = 0;
    m_send_count = 0;
    m_dump_interval = ESA_DEFAULT_DUMP_INTERVAL_;
    m_retry_count = 0;

    m_skip_flag = false;
    m_timer_id = -1;
    m_timer_triggered = 0;
    m_redis_conn_time = 0;

    m_self_index = (1 << index);

    m_dump_all = 0;
    m_dump_fin = true;

    struct tm time_st;
    time_t time_sec = time(NULL);

    localtime_r(&time_sec, &time_st);
    m_db_index = time_st.tm_mday;
    INFO_LOG("db_index = %d", m_db_index);
    char buf[8];
    if (time_st.tm_hour < 10)
    {
        snprintf(buf, 8, "0%u00", time_st.tm_hour);
    }
    else
    {
        snprintf(buf, 8, "%u00", time_st.tm_hour);
    }
    m_hour_mark = buf;
    INFO_LOG("date = %s", m_hour_mark.c_str());

    m_p_redis_conn = p_conn;
    m_db_selected = false;
    m_p_channel_service = p_channel_service;
    m_p_heap_manager = p_heap_manager;
    m_connecting = false;

    m_p_signal_channel = m_p_channel_service->NewSoftSignalChannel();
    m_p_signal_channel->Register(1, this);
    m_p_signal_channel->Register(2, this);

    m_p_timer = &(m_p_channel_service->GetTimer());

    m_b_it_is_begin = true;
    m_heap_index = 0;

    m_p_domain = NULL;
    m_session_manager.Init();
    m_p_session = NULL;
    init_redis_cmds();

    m_p_logger = p_logger;
    m_domain_index = 0;
    m_load_from_redis_flag = load_from_redis_flag;
    m_load_from_redis_stat = ESA_REDIS_READY_TO_LOAD_;
    m_last_dump_cnt = 0;

    //-- m_domain_it: do not init
    m_hour_changed = false;
    m_date_changed = false;
    m_new_date = m_db_index;

    m_signal_1_cnt = 0;
    m_signal_2_cnt = 0;
    return 0;
}

void AggregateRedisHandler::OnConnected(RedisConnection* conn)
{
    int status;
    INFO_LOG("Redis connected");
    m_connecting = false;
    m_redis_conn_time = 0;

    status = SelectDB();
    if (status != 0)
    {
        ERROR_LOG("select db failed");
        return;
    }
}

void AggregateRedisHandler::OnDisconnected(RedisConnection* conn)
{
    ERROR_LOG("Redis disconnected.");
    m_connecting = false;
    m_db_selected = false;
}

int AggregateRedisHandler::LogDomainData(DomainMap::iterator &it)
{
    //if ( 0 == (it->second->add_flag & m_self_index ))
    //{
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s %s %u", g_str_sadd.c_str(),
            g_str_key_dm.c_str(), it->first);

    Log2File(m_buf);
    m_send_count++;
    //}

    ESADomainItem dm_all;
    memset( &dm_all, 0x00, sizeof( ESADomainItem ) );

    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u ", g_str_hincrby.c_str(),
            RTA_REDIS_DM, it->first);
    char *p_buf = m_buf + strlen(m_buf);
    int len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

    if (0 != it->second->dm_guest.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                it->second->dm_guest.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_pv.c_str(),
                it->second->dm_guest.pv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.pv += it->second->dm_guest.pv;
    }

    if (0 != it->second->dm_guest.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                it->second->dm_guest.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_uv.c_str(),
                it->second->dm_guest.uv);
        Log2File(m_buf);
        m_send_count += 2;

        dm_all.uv += it->second->dm_guest.uv;
    }

    if (0 != it->second->dm_guest.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                it->second->dm_guest.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_vv.c_str(),
                it->second->dm_guest.vv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.vv += it->second->dm_guest.vv;
    }

    if (0 != it->second->dm_guest.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                it->second->dm_guest.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_iv.c_str(),
                it->second->dm_guest.iv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.iv += it->second->dm_guest.iv;
    }

    if (0 != it->second->dm_guest.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_thread.c_str(),
                it->second->dm_guest.thread);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_thread.c_str(),
                it->second->dm_guest.thread);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.thread += it->second->dm_guest.thread;
    }

    if (0 != it->second->dm_guest.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_post.c_str(),
                it->second->dm_guest.post);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_post.c_str(),
                it->second->dm_guest.post );
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.post += it->second->dm_guest.post;
    }

    if (0 != it->second->dm_guest.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                it->second->dm_guest.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_time.c_str(),
                it->second->dm_guest.time);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.time += it->second->dm_guest.time;
    }

    if (0 != it->second->dm_guest.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                it->second->dm_guest.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_bounce.c_str(),
                it->second->dm_guest.bounce);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.bounce += it->second->dm_guest.bounce;
    }

    if (0 != it->second->dm_vip.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                it->second->dm_vip.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_pv.c_str(),
                it->second->dm_vip.pv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.pv += it->second->dm_vip.pv;
    }

    if (0 != it->second->dm_vip.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                it->second->dm_vip.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_uv.c_str(),
                it->second->dm_vip.uv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.uv += it->second->dm_vip.uv;
    }

    if (0 != it->second->dm_vip.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                it->second->dm_vip.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_vv.c_str(),
                it->second->dm_vip.vv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.vv += it->second->dm_vip.vv;
    }

    if (0 != it->second->dm_vip.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                it->second->dm_vip.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_iv.c_str(),
                it->second->dm_vip.iv);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.iv += it->second->dm_vip.iv;
    }
    if (0 != it->second->dm_vip.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_thread.c_str(),
                it->second->dm_vip.thread);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_thread.c_str(),
                it->second->dm_vip.thread);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.thread += it->second->dm_vip.thread;
    }

    if (0 != it->second->dm_vip.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_post.c_str(),
                it->second->dm_vip.post);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_post.c_str(),
                it->second->dm_vip.post);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.post += it->second->dm_vip.post;
    }

    if (0 != it->second->dm_vip.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                it->second->dm_vip.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_time.c_str(),
                it->second->dm_vip.time);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.time += it->second->dm_vip.time;
    }

    if (0 != it->second->dm_vip.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                it->second->dm_vip.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_bounce.c_str(),
                it->second->dm_vip.bounce);
        Log2File(m_buf);
        m_send_count += 2;
        
        dm_all.bounce += it->second->dm_vip.bounce;
    }

    if (0 != it->second->vip)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_cnt.c_str(), it->second->vip);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_cnt.c_str(),
                it->second->vip);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != it->second->old_vip)
    {
        snprintf(p_buf, len, "%s %d", g_str_old_vip_cnt.c_str(),
                it->second->old_vip);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_old_vip_cnt.c_str(),
                it->second->old_vip);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != it->second->guest)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_cnt.c_str(),
                it->second->guest);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_cnt.c_str(),
                it->second->guest);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != it->second->new_guest)
    {
        snprintf(p_buf, len, "%s %d", g_str_new_guest_cnt.c_str(),
                it->second->new_guest);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_new_guest_cnt.c_str(),
                it->second->new_guest);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(), dm_all.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_pv.c_str(),
                dm_all.pv );
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(), dm_all.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_uv.c_str(),
                dm_all.uv );
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(), dm_all.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_vv.c_str(),
                dm_all.vv );
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(), dm_all.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_iv.c_str(),
                dm_all.iv );
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_thread.c_str(), dm_all.thread);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_thread.c_str(),
                dm_all.thread );
        Log2File(m_buf);
        m_send_count += 2;
    }
    if (0 != dm_all.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_post.c_str(), dm_all.post);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_post.c_str(),
                dm_all.post );
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(), dm_all.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_time.c_str(),
                dm_all.time);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != dm_all.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(), dm_all.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_bounce.c_str(),
                dm_all.bounce);
        Log2File(m_buf);
        m_send_count += 2;
    }

    //LogDomainHourData(it);
    return 0;
}

int AggregateRedisHandler::DumpDomainData(DomainMap::iterator &it, bool log_flag )
{
    if (NULL == it->second)
    {
        return 0;
    }

    if (NULL != g_debug_stat_worker && m_self_index == 1)
    {
        g_debug_stat_worker->StatWriteEvent(it->first, it->second->dm_guest );
        g_debug_stat_worker->StatWriteEvent(it->first, it->second->dm_vip );
    }

    ESADomainItem dm_all;
    memset( &dm_all, 0x00, sizeof( ESADomainItem ) );

    //if ( 0 == (it->second->add_flag & m_self_index ))
    //{
    //    it->second->add_flag |= m_self_index; 
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", it->first);
    WriteToRedisSadd(g_str_sadd.c_str(), g_str_key_dm.c_str(), m_buf);
    //}

    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_DM,
            it->first);
    //---------------
    if (0 != it->second->dm_guest.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_pv.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.pv += it->second->dm_guest.pv;
    }

    //---------------
    if (0 != it->second->dm_guest.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_uv.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.uv += it->second->dm_guest.uv;
    }

    //---------------
    if (0 != it->second->dm_guest.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_vv.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.vv += it->second->dm_guest.vv;
    }

    //---------------
    if (0 != it->second->dm_guest.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_iv.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.iv += it->second->dm_guest.iv;
    }

    //---------------
    if (0 != it->second->dm_guest.thread)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                it->second->dm_guest.thread);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_thread.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_thread.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.thread += it->second->dm_guest.thread;
    }

    //---------------
    if (0 != it->second->dm_guest.post)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.post);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_post.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_post.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.post += it->second->dm_guest.post;
    }

    //---------------
    if (0 != it->second->dm_guest.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_time.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.time += it->second->dm_guest.time;
    }

    //---------------
    if (0 != it->second->dm_guest.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                it->second->dm_guest.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_bounce.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.bounce += it->second->dm_guest.bounce;
    }

    //---------------
    if (0 != it->second->dm_vip.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_pv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.pv += it->second->dm_vip.pv;
    }

    //---------------
    if (0 != it->second->dm_vip.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_uv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.uv += it->second->dm_vip.uv;
    }

    //---------------
    if (0 != it->second->dm_vip.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_vv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.vv += it->second->dm_vip.vv;
    }

    //---------------
    if (0 != it->second->dm_vip.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_iv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.iv += it->second->dm_vip.iv;
    }

    //---------------
    if (0 != it->second->dm_vip.thread)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.thread);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_thread.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_thread.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.thread += it->second->dm_vip.thread;
    }

    //---------------
    if (0 != it->second->dm_vip.post)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.post);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_post.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_post.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.post += it->second->dm_vip.post;
    }

    //---------------
    if (0 != it->second->dm_vip.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_time.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.time += it->second->dm_vip.time;
    }

    //---------------
    if (0 != it->second->dm_vip.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_bounce.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

        dm_all.bounce += it->second->dm_vip.bounce;
    }

    if (0 != dm_all.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_pv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);

    }

    //---------------
    if (0 != dm_all.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_uv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_vv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_iv.c_str(),
                m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.thread)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.thread);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_thread.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_thread.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.post)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.post);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_post.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_post.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_time.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //---------------
    if (0 != dm_all.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", dm_all.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_bounce.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_all_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != it->second->vip)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_cnt.c_str(), m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_vip_cnt.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != it->second->old_vip)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->old_vip);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_old_vip_cnt.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_old_vip_cnt.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != it->second->guest)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_cnt.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_guest_cnt.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != it->second->new_guest)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->new_guest);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_new_guest_cnt.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(), 
                g_str_new_guest_cnt.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    //DumpDomainHourData(it);
    return 0;
}

int AggregateRedisHandler::DumpDomainHourData(DomainMap::iterator &it)
{
/*
    char buf_feild[ESA_DEFAULT_BUF_LEN_];
    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_DM,
            it->first);
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_WRITE_);
    m_p_session->cmd.push_back(g_str_hmset);
    m_p_session->cmd.push_back(m_buf_key);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_pv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.pv);

    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_uv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.uv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_vv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.vv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_iv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.iv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_thread.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.thread);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_post.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.post);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_time.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.time);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_bounce.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_all_sum.bounce);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_pv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.pv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_uv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.uv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_vv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.vv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_iv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.iv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_thread.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.thread);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_post.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.post);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_time.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.time);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_bounce.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_vip_sum.bounce);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_pv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.pv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_uv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.uv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_vv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.vv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_iv.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.iv);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_thread.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            it->second->dm_guest_sum.thread);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_post.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.post);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_time.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->dm_guest_sum.time);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    snprintf(buf_feild, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_bounce.c_str());
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            it->second->dm_guest_sum.bounce);
    m_p_session->cmd.push_back(buf_feild);
    m_p_session->cmd.push_back(m_buf);

    m_p_session->p_reply->w_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisWriteCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
*/
    return 0;
}

int AggregateRedisHandler::LogDomainHourData(DomainMap::iterator &it)
{
/*
    if (false == m_hour_changed)
    {
        return 0;
    }

    char *p_buf = NULL;
    int len = 0;

    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %s:", g_str_hset.c_str(),
            RTA_REDIS_DM, it->second->domain, m_hour_mark.c_str());

    p_buf = m_buf + strlen(m_buf);
    len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

    //---------------
    if (0 != it->second->dm_all_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                it->second->dm_all_sum.pv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                it->second->dm_all_sum.uv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                it->second->dm_all_sum.vv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                it->second->dm_all_sum.iv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_thread.c_str(),
                it->second->dm_all_sum.thread);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_post.c_str(),
                it->second->dm_all_sum.post);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                it->second->dm_all_sum.time);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_all_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                it->second->dm_all_sum.bounce);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                it->second->dm_vip_sum.pv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                it->second->dm_vip_sum.uv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                it->second->dm_vip_sum.vv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                it->second->dm_vip_sum.iv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_thread.c_str(),
                it->second->dm_vip_sum.thread);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_post.c_str(),
                it->second->dm_vip_sum.post);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                it->second->dm_vip_sum.time);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_vip_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                it->second->dm_vip_sum.bounce);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                it->second->dm_guest_sum.pv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                it->second->dm_guest_sum.uv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                it->second->dm_guest_sum.vv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                it->second->dm_guest_sum.iv);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.thread)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_thread.c_str(),
                it->second->dm_guest_sum.thread);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.post)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_post.c_str(),
                it->second->dm_guest_sum.post);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                it->second->dm_guest_sum.time);
        Log2File(m_buf);
        m_send_count++;
    }

    //---------------
    if (0 != it->second->dm_guest_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                it->second->dm_guest_sum.bounce);
        Log2File(m_buf);
        m_send_count++;
    }
*/
    return 0;
}

int AggregateRedisHandler::LogPlateData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char* p_buf = NULL;
    int len = 0;
    ESAPlateMap::iterator it;
    for (it = m_p_domain->plate_map.begin(); it != m_p_domain->plate_map.end();
            it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_PLATE, m_p_domain->domain,
                it->first);
        Log2File(m_buf);
        m_send_count++;
        //}

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_PLATE, m_p_domain->domain,
                it->first);
        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        //---------------
        if (NULL != it->second->p_guest)
        {
            if (0 != it->second->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->thread)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_thread.c_str(),
                        it->second->p_guest->thread);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_post.c_str(),
                        it->second->p_guest->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->thread)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_thread.c_str(),
                        it->second->p_vip->thread);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_post.c_str(),
                        it->second->p_vip->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        //---------------
        if (NULL != it->second->p_all)
        {
            if (0 != it->second->p_all->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        it->second->p_all->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        it->second->p_all->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        it->second->p_all->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        it->second->p_all->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->thread)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_thread.c_str(),
                        it->second->p_all->thread);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_post.c_str(),
                        it->second->p_all->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        it->second->p_all->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        it->second->p_all->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpPlateData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    ESAPlateMap::iterator it;
    for (it = m_p_domain->plate_map.begin(); it != m_p_domain->plate_map.end();
            it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{
        //    it->second->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_PLATE,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", it->first);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u", RTA_REDIS_PLATE,
                m_p_domain->domain, it->first);

        if (it->second->p_guest != NULL)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->thread)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->thread);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_thread.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
            }
        }

        if (it->second->p_vip != NULL)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->thread)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->thread);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_thread.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
            }
        }

        if (it->second->p_all != NULL)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->thread)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->thread);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_thread.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::LogPageData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;
    ESAPageMap::iterator it;
    for (it = m_p_domain->page_map.begin(); it != m_p_domain->page_map.end();
            it++)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_PAGE, m_p_domain->domain,
                it->first);
        m_send_count++;

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_PAGE, m_p_domain->domain,
                it->first);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(p_buf, len, " %s %d", g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        it->second->p_all->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        it->second->p_all->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        it->second->p_all->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        it->second->p_all->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        it->second->p_all->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        it->second->p_all->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpPageData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    ESAPageMap::iterator it;
    for (it = m_p_domain->page_map.begin(); it != m_p_domain->page_map.end();
            it++)
    {
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_PAGE,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%llu", it->first);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%llu",
                RTA_REDIS_PAGE, m_p_domain->domain, it->first);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpThreadData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    ESAThreadMap::iterator it;
    for (it = m_p_domain->thread_map.begin();
            it != m_p_domain->thread_map.end(); it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{
        //    it->second->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_THREAD,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", it->first);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                RTA_REDIS_THREAD, m_p_domain->domain, it->first);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->post)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->post);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_post.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::LogThreadData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;
    ESAThreadMap::iterator it;
    for (it = m_p_domain->thread_map.begin();
            it != m_p_domain->thread_map.end(); it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_THREAD, m_p_domain->domain,
                it->first);
        m_send_count++;
        //}

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_THREAD, m_p_domain->domain,
                it->first);
        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_post.c_str(),
                        it->second->p_guest->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_post.c_str(),
                        it->second->p_vip->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        it->second->p_all->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        it->second->p_all->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        it->second->p_all->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        it->second->p_all->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->post)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_post.c_str(),
                        it->second->p_all->post);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        it->second->p_all->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        it->second->p_all->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

    }
    return 0;
}

int AggregateRedisHandler::DumpDMRefData(DomainMap::iterator &dm_it, bool log_flag )
{
    bool dump_hour_data = false;
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESADomainReferItem ref_all;
    ESADomainReferItem ref_all_sum;
    memset( &ref_all_sum, 0x00, sizeof( ESADomainReferItem ) );

    //int status;
    ESADomainReferMap::iterator it;
    for (it = m_p_domain->dm_ref.dm_ref_map.begin();
            it != m_p_domain->dm_ref.dm_ref_map.end(); it++)
    {
        dump_hour_data = false;
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                    RTA_REDIS_DM_REF, m_p_domain->domain);
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%llu", it->first);
            WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        }

        memset( &ref_all, 0x00, sizeof( ESADomainReferItem ) );

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%llu",
                RTA_REDIS_DM_REF, m_p_domain->domain, it->first);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);

                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_pv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.pv += it->second->p_guest->pv;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_uv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.uv += it->second->p_guest->uv;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_vv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.vv += it->second->p_guest->vv;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_iv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.iv += it->second->p_guest->iv;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_time.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.time += it->second->p_guest->time;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_guest_bounce.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.bounce += it->second->p_guest->bounce;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_pv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.pv += it->second->p_vip->pv;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_uv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.uv += it->second->p_vip->uv;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_vv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.vv += it->second->p_vip->vv;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_iv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.iv += it->second->p_vip->iv;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_time.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.time += it->second->p_vip->time;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
                snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                        g_str_vip_bounce.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
                ref_all.bounce += it->second->p_vip->bounce;
            }
        }

        //---------------
        if (0 != ref_all.pv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.pv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_pv.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_pv.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }

        //---------------
        if (0 != ref_all.uv )
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.uv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_uv.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_uv.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }

        //---------------
        if (0 != ref_all.vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_vv.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_vv.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }

        //---------------
        if (0 != ref_all.iv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.iv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_iv.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_iv.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }

        //---------------
        if (0 != ref_all.time)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.time);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_time.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_time.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }

        //---------------
        if (0 != ref_all.bounce)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all.bounce);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_bounce.c_str(), m_buf);
            snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                    g_str_all_bounce.c_str());
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    m_buf_field, m_buf);
        }
    }

    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:1",
            RTA_REDIS_DM_REF, m_p_domain->domain);

    if (0 != m_p_domain->dm_ref.ref_guest.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.pv += m_p_domain->dm_ref.ref_guest.pv;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.uv += m_p_domain->dm_ref.ref_guest.uv;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.vv += m_p_domain->dm_ref.ref_guest.vv;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.iv += m_p_domain->dm_ref.ref_guest.iv;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.time += m_p_domain->dm_ref.ref_guest.time;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_guest.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_guest_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.bounce += m_p_domain->dm_ref.ref_guest.bounce;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.pv += m_p_domain->dm_ref.ref_vip.pv;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.uv += m_p_domain->dm_ref.ref_vip.uv;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.vv += m_p_domain->dm_ref.ref_vip.vv;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.iv += m_p_domain->dm_ref.ref_vip.iv;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.time += m_p_domain->dm_ref.ref_vip.time;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_vip_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
        ref_all_sum.bounce += m_p_domain->dm_ref.ref_vip.bounce;
    }

    if (0 != ref_all_sum.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != ref_all_sum.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != ref_all_sum.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != ref_all_sum.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != ref_all_sum.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != ref_all_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", ref_all_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
                g_str_all_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }
    return 0;
}

int AggregateRedisHandler::DumpDMRefHourData(DomainMap::iterator &dm_it)
{
/*
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    ESADomainReferMap::iterator it;
    for (it = m_p_domain->dm_ref.dm_ref_map.begin();
            it != m_p_domain->dm_ref.dm_ref_map.end(); it++)
    {
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%llu",
                RTA_REDIS_DM_REF, m_p_domain->domain, it->first);

        m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_WRITE_);
        m_p_session->cmd.push_back(g_str_hmset);
        m_p_session->cmd.push_back(m_buf_key);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_pv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.pv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_uv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.uv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_vv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.vv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_iv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.iv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_time.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.time);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_bounce.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->vip_sum.bounce);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_pv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest_sum.pv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_uv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest_sum.uv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_vv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest_sum.vv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_iv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest_sum.iv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_time.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->guest_sum.time);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_bounce.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                it->second->guest_sum.bounce);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_pv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.pv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_uv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.uv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_vv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.vv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_iv.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.iv);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_time.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.time);
        m_p_session->cmd.push_back(m_buf);

        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_bounce.c_str());
        m_p_session->cmd.push_back(m_buf_field);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", it->second->all_sum.bounce);
        m_p_session->cmd.push_back(m_buf);

        m_p_session->p_reply->w_reply.ptr = (void*) this;
        m_p_redis_conn->AsyncCommand(AggregateRedisWriteCallBack,
                m_p_session->p_reply, m_p_session->cmd);
        m_send_count++;
        //print_vector( m_p_session->cmd );
    }

    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:1", RTA_REDIS_DM_REF,
            m_p_domain->domain);

    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_WRITE_);
    m_p_session->cmd.push_back(g_str_hmset);
    m_p_session->cmd.push_back(m_buf_key);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_pv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.pv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_uv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.uv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_vv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.vv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_iv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_vip.iv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_time.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_vip.time);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_vip_bounce.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_vip.bounce);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_pv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.pv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_uv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.uv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_vv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.vv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_iv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.iv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_time.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.time);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_guest_bounce.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_guest.bounce);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_pv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_all.pv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_uv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_all.uv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_vv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_all.vv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_iv.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", m_p_domain->dm_ref.ref_all.iv);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_time.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_all.time);
    m_p_session->cmd.push_back(m_buf);

    snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", m_hour_mark.c_str(),
            g_str_all_bounce.c_str());
    m_p_session->cmd.push_back(m_buf_field);
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
            m_p_domain->dm_ref.ref_all.bounce);
    m_p_session->cmd.push_back(m_buf);

    m_p_session->p_reply->w_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisWriteCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_p_session->cmd );
*/
    return 0;
}

int AggregateRedisHandler::LogDMRefData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;

    ESADomainReferItem ref_all;
    ESADomainReferItem ref_all_sum;
    memset( &ref_all_sum, 0x00, sizeof( ESADomainReferItem ) );

    ESADomainReferMap::iterator it;
    for (it = m_p_domain->dm_ref.dm_ref_map.begin();
            it != m_p_domain->dm_ref.dm_ref_map.end(); it++)
    {
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                    g_str_sadd.c_str(), RTA_REDIS_DM_REF, m_p_domain->domain,
                    it->first);
            Log2File(m_buf);
            m_send_count++;
        }

        memset( &ref_all, 0x00, sizeof( ESADomainReferItem ) );
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_DM_REF, m_p_domain->domain,
                it->first);
        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        //---------------
        if (NULL != it->second->p_guest)
        {
            if (0 != it->second->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                ref_all.pv += it->second->p_guest->pv;  
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                ref_all.uv += it->second->p_guest->uv;  
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                ref_all.vv += it->second->p_guest->vv;  
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                ref_all.iv += it->second->p_guest->iv;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                ref_all.time += it->second->p_guest->time;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                ref_all.bounce += it->second->p_guest->bounce;
                m_send_count += 2;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                ref_all.pv += it->second->p_vip->pv;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                ref_all.uv += it->second->p_vip->uv;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                ref_all.vv += it->second->p_vip->vv;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                ref_all.iv += it->second->p_vip->iv;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                ref_all.time += it->second->p_vip->time;
                m_send_count += 2;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                ref_all.bounce += it->second->p_vip->bounce;
                m_send_count += 2;
            }
        }

        //---------------
        if (0 != ref_all.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                    ref_all.pv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_pv.c_str(),
                    ref_all.pv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        //---------------
        if (0 != ref_all.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                    ref_all.uv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_uv.c_str(),
                    ref_all.uv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        //---------------
        if (0 != ref_all.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                    ref_all.vv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_vv.c_str(),
                    ref_all.vv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        //---------------
        if (0 != ref_all.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                    ref_all.iv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_iv.c_str(),
                    ref_all.iv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        //---------------
        if (0 != ref_all.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                    ref_all.time);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_time.c_str(),
                    ref_all.time);
            Log2File(m_buf);
            m_send_count += 2;
        }

        //---------------
        if (0 != ref_all.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                    ref_all.bounce);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_bounce.c_str(),
                    ref_all.bounce);
            Log2File(m_buf);
            m_send_count += 2;
        }
    }

    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:1 ",
            g_str_hincrby.c_str(), RTA_REDIS_DM_REF, m_p_domain->domain);
    p_buf = m_buf + strlen(m_buf);
    len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

    if (0 != m_p_domain->dm_ref.ref_guest.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                m_p_domain->dm_ref.ref_guest.pv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_pv.c_str(),
                m_p_domain->dm_ref.ref_guest.pv );
        Log2File(m_buf);
        ref_all_sum.pv += m_p_domain->dm_ref.ref_guest.pv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                m_p_domain->dm_ref.ref_guest.uv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_uv.c_str(),
                m_p_domain->dm_ref.ref_guest.uv );
        Log2File(m_buf);
        ref_all_sum.uv += m_p_domain->dm_ref.ref_guest.uv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                m_p_domain->dm_ref.ref_guest.vv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_vv.c_str(),
                m_p_domain->dm_ref.ref_guest.vv );
        Log2File(m_buf);
        ref_all_sum.vv += m_p_domain->dm_ref.ref_guest.vv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                m_p_domain->dm_ref.ref_guest.iv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_iv.c_str(),
                m_p_domain->dm_ref.ref_guest.iv );
        Log2File(m_buf);
        ref_all_sum.iv += m_p_domain->dm_ref.ref_guest.iv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                m_p_domain->dm_ref.ref_guest.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_time.c_str(),
                m_p_domain->dm_ref.ref_guest.time);
        Log2File(m_buf);
        ref_all_sum.time += m_p_domain->dm_ref.ref_guest.time;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_guest.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                m_p_domain->dm_ref.ref_guest.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_guest_bounce.c_str(),
                m_p_domain->dm_ref.ref_guest.bounce);
        Log2File(m_buf);
        ref_all_sum.bounce += m_p_domain->dm_ref.ref_guest.bounce;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                m_p_domain->dm_ref.ref_vip.pv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_pv.c_str(),
                m_p_domain->dm_ref.ref_vip.pv );
        Log2File(m_buf);
        ref_all_sum.pv += m_p_domain->dm_ref.ref_vip.pv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                m_p_domain->dm_ref.ref_vip.uv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_uv.c_str(),
                m_p_domain->dm_ref.ref_vip.uv );
        Log2File(m_buf);
        ref_all_sum.uv += m_p_domain->dm_ref.ref_vip.uv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                m_p_domain->dm_ref.ref_vip.vv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_vv.c_str(),
                m_p_domain->dm_ref.ref_vip.vv );
        Log2File(m_buf);
        ref_all_sum.vv += m_p_domain->dm_ref.ref_vip.vv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                m_p_domain->dm_ref.ref_vip.iv );
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_iv.c_str(),
                m_p_domain->dm_ref.ref_vip.iv );
        Log2File(m_buf);
        ref_all_sum.iv += m_p_domain->dm_ref.ref_vip.iv;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                m_p_domain->dm_ref.ref_vip.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_time.c_str(),
                m_p_domain->dm_ref.ref_vip.time);
        Log2File(m_buf);
        ref_all_sum.time += m_p_domain->dm_ref.ref_vip.time;
        m_send_count += 2;
    }

    if (0 != m_p_domain->dm_ref.ref_vip.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                m_p_domain->dm_ref.ref_vip.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_vip_bounce.c_str(),
                m_p_domain->dm_ref.ref_vip.bounce);
        Log2File(m_buf);
        ref_all_sum.bounce += m_p_domain->dm_ref.ref_vip.bounce;
        m_send_count += 2;
    }

    if (0 != ref_all_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                ref_all_sum.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_pv.c_str(),
                ref_all_sum.pv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != ref_all_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                ref_all_sum.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_uv.c_str(),
                ref_all_sum.uv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != ref_all_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                ref_all_sum.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_vv.c_str(),
                ref_all_sum.vv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != ref_all_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                ref_all_sum.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_iv.c_str(),
                ref_all_sum.iv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != ref_all_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                ref_all_sum.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_time.c_str(),
                ref_all_sum.time);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != ref_all_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                ref_all_sum.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(), g_str_all_bounce.c_str(),
                ref_all_sum.bounce);
        Log2File(m_buf);
        m_send_count += 2;
    }
    
    return 0;
}

int AggregateRedisHandler::LogDMRefHourData(DomainMap::iterator &dm_it)
{
/*
    if (true != m_hour_changed)
    {
        return 0;
    }

    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESADomainReferMap::iterator it;
    for (it = m_p_domain->dm_ref.dm_ref_map.begin();
            it != m_p_domain->dm_ref.dm_ref_map.end(); it++)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%llu"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d"
                " %s:%s %d", g_str_hmset.c_str(), RTA_REDIS_DM_REF,
                m_p_domain->domain, it->first, m_hour_mark.c_str(),
                g_str_vip_pv.c_str(), it->second->vip_sum.pv,
                m_hour_mark.c_str(), g_str_vip_uv.c_str(),
                it->second->vip_sum.uv, m_hour_mark.c_str(),
                g_str_vip_vv.c_str(), it->second->vip_sum.vv,
                m_hour_mark.c_str(), g_str_vip_iv.c_str(),
                it->second->vip_sum.iv, m_hour_mark.c_str(),
                g_str_vip_time.c_str(), it->second->vip_sum.time,
                m_hour_mark.c_str(), g_str_vip_bounce.c_str(),
                it->second->vip_sum.bounce, m_hour_mark.c_str(),
                g_str_guest_pv.c_str(), it->second->guest_sum.pv,
                m_hour_mark.c_str(), g_str_guest_uv.c_str(),
                it->second->guest_sum.uv, m_hour_mark.c_str(),
                g_str_guest_vv.c_str(), it->second->guest_sum.vv,
                m_hour_mark.c_str(), g_str_guest_iv.c_str(),
                it->second->guest_sum.iv, m_hour_mark.c_str(),
                g_str_guest_time.c_str(), it->second->guest_sum.time,
                m_hour_mark.c_str(), g_str_guest_bounce.c_str(),
                it->second->guest_sum.bounce, m_hour_mark.c_str(),
                g_str_all_pv.c_str(), it->second->all_sum.pv,
                m_hour_mark.c_str(), g_str_all_uv.c_str(),
                it->second->all_sum.uv, m_hour_mark.c_str(),
                g_str_all_vv.c_str(), it->second->all_sum.vv,
                m_hour_mark.c_str(), g_str_all_iv.c_str(),
                it->second->all_sum.iv, m_hour_mark.c_str(),
                g_str_all_time.c_str(), it->second->all_sum.time,
                m_hour_mark.c_str(), g_str_all_bounce.c_str(),
                it->second->all_sum.bounce);

        Log2File(m_buf);
        m_send_count++;
    }

    snprintf(
            m_buf,
            ESA_DEFAULT_BUF_LEN_,
            "%s 0:%u:%u:1" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d" \
" %s:%s %d",
            g_str_hmset.c_str(), RTA_REDIS_DM_REF, m_p_domain->domain,
            m_hour_mark.c_str(), g_str_vip_pv.c_str(),
            m_p_domain->dm_ref.ref_vip.pv, m_hour_mark.c_str(),
            g_str_vip_uv.c_str(), m_p_domain->dm_ref.ref_vip.uv,
            m_hour_mark.c_str(), g_str_vip_vv.c_str(),
            m_p_domain->dm_ref.ref_vip.vv, m_hour_mark.c_str(),
            g_str_vip_iv.c_str(), m_p_domain->dm_ref.ref_vip.iv,
            m_hour_mark.c_str(), g_str_vip_time.c_str(),
            m_p_domain->dm_ref.ref_vip.time, m_hour_mark.c_str(),
            g_str_vip_bounce.c_str(), m_p_domain->dm_ref.ref_vip.bounce,
            m_hour_mark.c_str(), g_str_guest_pv.c_str(),
            m_p_domain->dm_ref.ref_guest.pv, m_hour_mark.c_str(),
            g_str_guest_uv.c_str(), m_p_domain->dm_ref.ref_guest.uv,
            m_hour_mark.c_str(), g_str_guest_vv.c_str(),
            m_p_domain->dm_ref.ref_guest.vv, m_hour_mark.c_str(),
            g_str_guest_iv.c_str(), m_p_domain->dm_ref.ref_guest.iv,
            m_hour_mark.c_str(), g_str_guest_time.c_str(),
            m_p_domain->dm_ref.ref_guest.time, m_hour_mark.c_str(),
            g_str_guest_bounce.c_str(), m_p_domain->dm_ref.ref_guest.bounce,
            m_hour_mark.c_str(), g_str_all_pv.c_str(),
            m_p_domain->dm_ref.ref_all.pv, m_hour_mark.c_str(),
            g_str_all_uv.c_str(), m_p_domain->dm_ref.ref_all.uv,
            m_hour_mark.c_str(), g_str_all_vv.c_str(),
            m_p_domain->dm_ref.ref_all.vv, m_hour_mark.c_str(),
            g_str_all_iv.c_str(), m_p_domain->dm_ref.ref_all.iv,
            m_hour_mark.c_str(), g_str_all_time.c_str(),
            m_p_domain->dm_ref.ref_all.time, m_hour_mark.c_str(),
            g_str_all_bounce.c_str(), m_p_domain->dm_ref.ref_all.bounce);
    Log2File(m_buf);
    m_send_count++;
*/
    return 0;
}

int AggregateRedisHandler::LogSearchEngineData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESASearchKeyWordItem key_word_all;
    ESASearchKeyWordItem search_engine_guest;
    ESASearchKeyWordItem search_engine_vip;
    ESASearchKeyWordItem search_engine_all;

    ESASearchKeyWordItem search_engine_guest_sum;
    ESASearchKeyWordItem search_engine_vip_sum;
    ESASearchKeyWordItem search_engine_all_sum;

    memset( &search_engine_guest_sum, 0x00, sizeof(ESASearchKeyWordItem) );
    memset( &search_engine_vip_sum, 0x00, sizeof(ESASearchKeyWordItem) );
    memset( &search_engine_all_sum, 0x00, sizeof(ESASearchKeyWordItem) );

    int size = m_p_domain->seach_engine_vec.size();
    int pos = 0;
    char *p_buf = NULL;
    int len = 0;
    ESASearchEngine *p_item = NULL;
    for (; pos < size; pos++)
    {
        p_item = m_p_domain->seach_engine_vec[pos];
        if (p_item == NULL)
        {
            continue;
        }

        //if ( 0 == ( p_item->add_flag & m_self_index ) )
        //{ 
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_SEARCH_ENGINE, m_p_domain->domain,
                p_item->searchengine);
        Log2File(m_buf);
        m_send_count++;
        //}

        memset( &search_engine_guest, 0x00, sizeof( ESASearchKeyWordItem ) );
        memset( &search_engine_vip, 0x00, sizeof( ESASearchKeyWordItem ) );
        memset( &search_engine_all, 0x00, sizeof( ESASearchKeyWordItem ) );

        ESASearchKeyWordBasicMap::iterator it;
        for (it = p_item->m_keyword_map.begin();
                it != p_item->m_keyword_map.end(); it++)
        {
            if (it->first != 0 && it->first != 1)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u %llu",
                        g_str_sadd.c_str(), RTA_REDIS_SEARCH_KEYWORD,
                        m_p_domain->domain, p_item->searchengine, it->first);
                Log2File(m_buf);
                m_send_count++;
            }

            memset( &key_word_all, 0x00, sizeof(ESASearchKeyWordItem) );

            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u:%llu ",
                    g_str_hincrby.c_str(), RTA_REDIS_SEARCH_KEYWORD,
                    m_p_domain->domain, p_item->searchengine, it->first);

            p_buf = m_buf + strlen(m_buf);
            len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

            if (NULL != it->second->p_guest)
            {
                //---------------
                if (0 != it->second->p_guest->pv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                            it->second->p_guest->pv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.pv += it->second->p_guest->pv;
                    search_engine_guest.pv += it->second->p_guest->pv;
                    search_engine_guest_sum.pv += it->second->p_guest->pv;
                }

                //---------------
                if (0 != it->second->p_guest->uv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                            it->second->p_guest->uv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.uv += it->second->p_guest->uv;
                    search_engine_guest.uv += it->second->p_guest->uv;
                    search_engine_guest_sum.uv += it->second->p_guest->uv;
                }

                //---------------
                if (0 != it->second->p_guest->vv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                            it->second->p_guest->vv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.vv += it->second->p_guest->vv;
                    search_engine_guest.vv += it->second->p_guest->vv;
                    search_engine_guest_sum.vv += it->second->p_guest->vv;
                }

                //---------------
                if (0 != it->second->p_guest->iv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                            it->second->p_guest->iv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.iv += it->second->p_guest->iv;
                    search_engine_guest.iv += it->second->p_guest->iv;
                    search_engine_guest_sum.iv += it->second->p_guest->iv;
                }

                //---------------
                if (0 != it->second->p_guest->time)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                            it->second->p_guest->time);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.time += it->second->p_guest->time;
                    search_engine_guest.time += it->second->p_guest->time;
                    search_engine_guest_sum.time += it->second->p_guest->time;
                }

                //---------------
                if (0 != it->second->p_guest->bounce)
                {
                    snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                            it->second->p_guest->bounce);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.bounce += it->second->p_guest->bounce;
                    search_engine_guest.bounce += it->second->p_guest->bounce;
                    search_engine_guest_sum.bounce += it->second->p_guest->bounce;
                }

                //---------------
            }

            if (NULL != it->second->p_vip)
            {

                if (0 != it->second->p_vip->pv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                            it->second->p_vip->pv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.pv += it->second->p_vip->pv;
                    search_engine_vip.pv += it->second->p_vip->pv;
                    search_engine_vip_sum.pv += it->second->p_vip->pv;
                }
                //---------------
                if (0 != it->second->p_vip->uv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                            it->second->p_vip->uv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.uv += it->second->p_vip->uv;
                    search_engine_vip.uv += it->second->p_vip->uv;
                    search_engine_vip_sum.uv += it->second->p_vip->uv;
                }

                //---------------
                if (0 != it->second->p_vip->vv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                            it->second->p_vip->vv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.vv += it->second->p_vip->vv;
                    search_engine_vip.vv += it->second->p_vip->vv;
                    search_engine_vip_sum.vv += it->second->p_vip->vv;
                }

                //---------------
                if (0 != it->second->p_vip->iv)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                            it->second->p_vip->iv);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.iv += it->second->p_vip->iv;
                    search_engine_vip.iv += it->second->p_vip->iv;
                    search_engine_vip_sum.iv += it->second->p_vip->iv;
                }

                //---------------
                if (0 != it->second->p_vip->time)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                            it->second->p_vip->time);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.time += it->second->p_vip->time;
                    search_engine_vip.time += it->second->p_vip->time;
                    search_engine_vip_sum.time += it->second->p_vip->time;
                }

                //---------------
                if (0 != it->second->p_vip->bounce)
                {
                    snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                            it->second->p_vip->bounce);
                    Log2File(m_buf);
                    m_send_count++;
                    key_word_all.bounce += it->second->p_vip->bounce;
                    search_engine_vip.bounce += it->second->p_vip->bounce;
                    search_engine_vip_sum.bounce += it->second->p_vip->bounce;
                }
            }

            //---------------
            if (0 != key_word_all.pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        key_word_all.pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != key_word_all.uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        key_word_all.uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != key_word_all.vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        key_word_all.vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != key_word_all.iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        key_word_all.iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != key_word_all.time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        key_word_all.time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != key_word_all.bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        key_word_all.bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u:0 ",
                g_str_hincrby.c_str(), RTA_REDIS_SEARCH_KEYWORD,
                m_p_domain->domain, p_item->searchengine ); 

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        if (0 != search_engine_guest.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                    search_engine_guest.pv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_pv.c_str(),
                    search_engine_guest.pv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_guest.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                    search_engine_guest.uv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_uv.c_str(),
                    search_engine_guest.uv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_guest.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                    search_engine_guest.vv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_vv.c_str(),
                    search_engine_guest.vv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_guest.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                    search_engine_guest.iv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_iv.c_str(),
                    search_engine_guest.iv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_guest.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                    search_engine_guest.time);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_time.c_str(),
                    search_engine_guest.time);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_guest.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                    search_engine_guest.bounce);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_bounce.c_str(),
                    search_engine_guest.bounce);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                    search_engine_vip.pv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_pv.c_str(),
                    search_engine_vip.pv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                    search_engine_vip.uv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_uv.c_str(),
                    search_engine_vip.uv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                    search_engine_vip.vv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_vv.c_str(),
                    search_engine_vip.vv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                    search_engine_vip.iv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_iv.c_str(),
                    search_engine_vip.iv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                    search_engine_vip.time);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_time.c_str(),
                    search_engine_vip.time);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_vip.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                    search_engine_vip.bounce);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_bounce.c_str(),
                    search_engine_vip.bounce);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                    search_engine_all.pv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_pv.c_str(),
                    search_engine_all.pv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                    search_engine_all.uv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_uv.c_str(),
                    search_engine_all.uv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                    search_engine_all.vv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_vv.c_str(),
                    search_engine_all.vv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                    search_engine_all.iv);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_iv.c_str(),
                    search_engine_all.iv);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                    search_engine_all.time);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_time.c_str(),
                    search_engine_all.time);
            Log2File(m_buf);
            m_send_count += 2;
        }

        if (0 != search_engine_all.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                    search_engine_all.bounce);
            Log2File(m_buf);
            snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_bounce.c_str(),
                    search_engine_all.bounce);
            Log2File(m_buf);
            m_send_count += 2;
        }
    }

    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:0:0 ",
            g_str_hincrby.c_str(), RTA_REDIS_SEARCH_KEYWORD,
            m_p_domain->domain);

    p_buf = m_buf + strlen(m_buf);
    len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

    if (0 != search_engine_guest_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                search_engine_guest_sum.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_pv.c_str(),
                search_engine_guest_sum.pv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_guest_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                search_engine_guest_sum.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_uv.c_str(),
                search_engine_guest_sum.uv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_guest_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                search_engine_guest_sum.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_vv.c_str(),
                search_engine_guest_sum.vv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_guest_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                search_engine_guest_sum.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_iv.c_str(),
                search_engine_guest_sum.iv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_guest_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                search_engine_guest_sum.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_time.c_str(),
                search_engine_guest_sum.time);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_guest_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                search_engine_guest_sum.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_guest_bounce.c_str(),
                search_engine_guest_sum.bounce);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                search_engine_vip_sum.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_pv.c_str(),
                search_engine_vip_sum.pv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                search_engine_vip_sum.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_uv.c_str(),
                search_engine_vip_sum.uv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                search_engine_vip_sum.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_vv.c_str(),
                search_engine_vip_sum.vv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                search_engine_vip_sum.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_iv.c_str(),
                search_engine_vip_sum.iv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                search_engine_vip_sum.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_time.c_str(),
                search_engine_vip_sum.time);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_vip_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                search_engine_vip_sum.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_vip_bounce.c_str(),
                search_engine_vip_sum.bounce);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                search_engine_all_sum.pv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_pv.c_str(),
                search_engine_all_sum.pv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                search_engine_all_sum.uv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_uv.c_str(),
                search_engine_all_sum.uv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                search_engine_all_sum.vv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_vv.c_str(),
                search_engine_all_sum.vv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                search_engine_all_sum.iv);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_iv.c_str(),
                search_engine_all_sum.iv);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                search_engine_all_sum.time);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_time.c_str(),
                search_engine_all_sum.time);
        Log2File(m_buf);
        m_send_count += 2;
    }

    if (0 != search_engine_all_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                search_engine_all_sum.bounce);
        Log2File(m_buf);
        snprintf(p_buf, len, "%s:%s %d", m_hour_mark.c_str(),g_str_all_bounce.c_str(),
                search_engine_all_sum.bounce);
        Log2File(m_buf);
        m_send_count += 2;
    }

    return 0;
}

int AggregateRedisHandler::DumpSearchEngineData(DomainMap::iterator &dm_it, bool log_flag)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESASearchKeyWordItem key_word_all;
    ESASearchKeyWordItem search_engine_guest;
    ESASearchKeyWordItem search_engine_vip;
    ESASearchKeyWordItem search_engine_all;

    ESASearchKeyWordItem search_engine_guest_sum;
    ESASearchKeyWordItem search_engine_vip_sum;
    ESASearchKeyWordItem search_engine_all_sum;

    memset( &search_engine_guest_sum, 0x00, sizeof(ESASearchKeyWordItem) );
    memset( &search_engine_vip_sum, 0x00, sizeof(ESASearchKeyWordItem) );
    memset( &search_engine_all_sum, 0x00, sizeof(ESASearchKeyWordItem) );

    //int status;
    int size = m_p_domain->seach_engine_vec.size();
    int pos = 0;
    ESASearchEngine *p_item = NULL;
    for (; pos < size; pos++)
    {
        p_item = m_p_domain->seach_engine_vec[pos];
        if (p_item == NULL)
        {
            continue;
        }
        //if ( 0 == ( p_item->add_flag & m_self_index ) )
        //{ 
        //    p_item->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                RTA_REDIS_SEARCH_ENGINE, m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", p_item->searchengine);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}
        
        memset( &search_engine_guest, 0x00, sizeof( ESASearchKeyWordItem ) );
        memset( &search_engine_vip, 0x00, sizeof( ESASearchKeyWordItem ) );
        memset( &search_engine_all, 0x00, sizeof( ESASearchKeyWordItem ) );

        ESASearchKeyWordBasicMap::iterator it;
        for (it = p_item->m_keyword_map.begin();
                it != p_item->m_keyword_map.end(); it++)
        {
            if ( it->first != 1 && it->first != 0)
            {
                snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                        RTA_REDIS_SEARCH_KEYWORD, m_p_domain->domain,
                        p_item->searchengine);
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%llu", it->first);
                WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
            }

            memset( &key_word_all, 0x00, sizeof(ESASearchKeyWordItem) );

            snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u:%llu",
                    RTA_REDIS_SEARCH_KEYWORD, m_p_domain->domain,
                    p_item->searchengine, it->first);

            if (NULL != it->second->p_guest)
            {
                //---------------
                if (0 != it->second->p_guest->pv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->pv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_pv.c_str(), m_buf);
                    key_word_all.pv += it->second->p_guest->pv;
    
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.pv += it->second->p_guest->pv;
                        search_engine_all_sum.pv += it->second->p_guest->pv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_pv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_guest->uv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->uv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_uv.c_str(), m_buf);
                    key_word_all.uv += it->second->p_guest->uv;
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.uv += it->second->p_guest->uv;
                        search_engine_all_sum.uv += it->second->p_guest->uv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_uv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_guest->vv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->vv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_vv.c_str(), m_buf);
                    key_word_all.vv += it->second->p_guest->vv;
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.vv += it->second->p_guest->vv;
                        search_engine_all_sum.vv += it->second->p_guest->vv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_vv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_guest->iv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->iv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_iv.c_str(), m_buf);
                    key_word_all.iv += it->second->p_guest->iv;
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.iv += it->second->p_guest->iv;
                        search_engine_all_sum.iv += it->second->p_guest->iv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_iv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_guest->time)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->time);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_time.c_str(), m_buf);
                    key_word_all.time += it->second->p_guest->time;
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.time += it->second->p_guest->time;
                        search_engine_all_sum.time += it->second->p_guest->time;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_time.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_guest->bounce)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_guest->bounce);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_guest_bounce.c_str(), m_buf);
                    key_word_all.bounce += it->second->p_guest->bounce;
                    if ( 0 == it->first )
                    {
                        search_engine_guest_sum.bounce += it->second->p_guest->bounce;
                        search_engine_all_sum.bounce += it->second->p_guest->bounce;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_guest_bounce.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }
            }

            if (NULL != it->second->p_vip)
            {
                //---------------
                if (0 != it->second->p_vip->pv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->pv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_pv.c_str(), m_buf);
                    key_word_all.pv += it->second->p_vip->pv;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.pv += it->second->p_vip->pv;
                        search_engine_all_sum.pv += it->second->p_vip->pv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_pv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_vip->uv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->uv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_uv.c_str(), m_buf);
                    key_word_all.uv += it->second->p_vip->uv;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.uv += it->second->p_vip->uv;
                        search_engine_all_sum.uv += it->second->p_vip->uv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_uv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_vip->vv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->vv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_vv.c_str(), m_buf);
                    key_word_all.vv += it->second->p_vip->vv;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.vv += it->second->p_vip->vv;
                        search_engine_all_sum.vv += it->second->p_vip->vv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_vv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_vip->iv)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->iv);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_iv.c_str(), m_buf);
                    key_word_all.iv += it->second->p_vip->iv;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.iv += it->second->p_vip->iv;
                        search_engine_all_sum.iv += it->second->p_vip->iv;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_iv.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_vip->time)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->time);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_time.c_str(), m_buf);
                    key_word_all.time += it->second->p_vip->time;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.time += it->second->p_vip->time;
                        search_engine_all_sum.time += it->second->p_vip->time;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_time.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }

                //---------------
                if (0 != it->second->p_vip->bounce)
                {
                    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                            it->second->p_vip->bounce);
                    WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                            g_str_vip_bounce.c_str(), m_buf);
                    key_word_all.bounce += it->second->p_vip->bounce;
                    if ( 0 == it->first )
                    {
                        search_engine_vip_sum.bounce += it->second->p_vip->bounce;
                        search_engine_all_sum.bounce += it->second->p_vip->bounce;
                        snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                                m_hour_mark.c_str(),g_str_vip_bounce.c_str());
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                m_buf_field, m_buf);
                    }
                }
            }

            //---------------
            if (0 != key_word_all.pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_pv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }

            //---------------
            if (0 != key_word_all.uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_uv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }

            //---------------
            if (0 != key_word_all.vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_vv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }

            //---------------
            if (0 != key_word_all.iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_iv.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }

            //---------------
            if (0 != key_word_all.time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_time.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }

            //---------------
            if (0 != key_word_all.bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        key_word_all.bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
                snprintf( m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s", 
                        m_hour_mark.c_str(),g_str_all_bounce.c_str());
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        m_buf_field, m_buf);
            }
        }
    }

    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:0:0",
            RTA_REDIS_SEARCH_KEYWORD, m_p_domain->domain);

    if (0 != search_engine_guest_sum.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_guest_sum.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_guest_sum.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_guest_sum.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_guest_sum.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_guest_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_guest_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_guest_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_vip_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_vip_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_vip_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_pv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_pv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_uv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_uv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_vv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_vv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_iv.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_iv.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_time.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_time.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }

    if (0 != search_engine_all_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                search_engine_all_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_bounce.c_str(), m_buf);
        snprintf(m_buf_field, ESA_DEFAULT_BUF_LEN_, "%s:%s",
                m_hour_mark.c_str(), g_str_all_bounce.c_str());
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                m_buf_field, m_buf);
    }
    return 0;
}

int AggregateRedisHandler::DumpAreaData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    ESAAreaBasicMap::iterator it;
    for (it = m_p_domain->area_map.begin(); it != m_p_domain->area_map.end();
            it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{ 
        //    it->second->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_AREA,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", it->first);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u", RTA_REDIS_AREA,
                m_p_domain->domain, it->first);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        it->second->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::LogAreaData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;
    ESAAreaBasicMap::iterator it;
    for (it = m_p_domain->area_map.begin(); it != m_p_domain->area_map.end();
            it++)
    {
        //if ( 0 == ( it->second->add_flag & m_self_index ) )
        //{ 
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_AREA, m_p_domain->domain,
                it->first);
        Log2File(m_buf);
        m_send_count++;
        //}
        
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_AREA, m_p_domain->domain,
                it->first);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        if (NULL != it->second->p_guest)
        {
            //---------------
            if (0 != it->second->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        it->second->p_guest->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        it->second->p_guest->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        it->second->p_guest->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        it->second->p_guest->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        it->second->p_guest->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        it->second->p_guest->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != it->second->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        it->second->p_vip->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        it->second->p_vip->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        it->second->p_vip->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        it->second->p_vip->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        it->second->p_vip->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        it->second->p_vip->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != it->second->p_all->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        it->second->p_all->pv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        it->second->p_all->uv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        it->second->p_all->vv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        it->second->p_all->iv);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        it->second->p_all->time);
                Log2File(m_buf);
                m_send_count++;
            }

            //---------------
            if (0 != it->second->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        it->second->p_all->bounce);
                Log2File(m_buf);
                m_send_count++;
            }
        }
    }
    return 0;
}

int AggregateRedisHandler::LogIspData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    int size = m_p_domain->isp_vec.size();
    int pos = 0;
    char *p_buf = NULL;
    int len = 0;

    ESAIspItem isp_guest;
    ESAIspItem isp_vip;
    ESAIspItem isp_all;

    memset(&isp_guest, 0x00, sizeof(ESAIspItem));
    memset(&isp_vip, 0x00, sizeof(ESAIspItem));
    memset(&isp_all, 0x00, sizeof(ESAIspItem));

    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->isp_vec[pos])
        {
            continue;
        }

        //if ( 0 == ( (m_p_domain->isp_vec[pos])->add_flag & m_self_index ) )
        //{   
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_ISP, m_p_domain->domain,
                (m_p_domain->isp_vec[pos])->ispcode);
        //INFO_LOG("%s", m_buf );
        Log2File(m_buf);
        m_send_count++;
        //}

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_ISP, m_p_domain->domain,
                (m_p_domain->isp_vec[pos])->ispcode);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        //---------------
        if (NULL != (m_p_domain->isp_vec[pos])->p_guest)
        {
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->pv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;

                isp_guest.pv += (m_p_domain->isp_vec[pos])->p_guest->pv;
                isp_all.pv += (m_p_domain->isp_vec[pos])->p_guest->pv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->uv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_guest.uv += (m_p_domain->isp_vec[pos])->p_guest->uv;
                isp_all.uv += (m_p_domain->isp_vec[pos])->p_guest->uv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->vv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_guest.vv += (m_p_domain->isp_vec[pos])->p_guest->vv;
                isp_all.vv += (m_p_domain->isp_vec[pos])->p_guest->vv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->iv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_guest.iv += (m_p_domain->isp_vec[pos])->p_guest->iv;
                isp_all.iv += (m_p_domain->isp_vec[pos])->p_guest->iv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->time);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_guest.time += (m_p_domain->isp_vec[pos])->p_guest->time;
                isp_all.time += (m_p_domain->isp_vec[pos])->p_guest->time;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        (m_p_domain->isp_vec[pos])->p_guest->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_guest.bounce += (m_p_domain->isp_vec[pos])->p_guest->bounce;
                isp_all.bounce += (m_p_domain->isp_vec[pos])->p_guest->bounce;
            }
        }

        if (NULL != (m_p_domain->isp_vec[pos])->p_vip)
        {
            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->pv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.pv += (m_p_domain->isp_vec[pos])->p_vip->pv;
                isp_all.pv += (m_p_domain->isp_vec[pos])->p_vip->pv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->uv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.uv += (m_p_domain->isp_vec[pos])->p_vip->uv;
                isp_all.uv += (m_p_domain->isp_vec[pos])->p_vip->uv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->vv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.vv += (m_p_domain->isp_vec[pos])->p_vip->vv;
                isp_all.vv += (m_p_domain->isp_vec[pos])->p_vip->vv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->iv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.iv += (m_p_domain->isp_vec[pos])->p_vip->iv;
                isp_all.iv += (m_p_domain->isp_vec[pos])->p_vip->iv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->time);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.time += (m_p_domain->isp_vec[pos])->p_vip->time;
                isp_all.time += (m_p_domain->isp_vec[pos])->p_vip->time;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        (m_p_domain->isp_vec[pos])->p_vip->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                isp_vip.bounce += (m_p_domain->isp_vec[pos])->p_vip->bounce;
                isp_all.bounce += (m_p_domain->isp_vec[pos])->p_vip->bounce;
            }
        }

        //---------------
        if (NULL != (m_p_domain->isp_vec[pos])->p_all)
        {
            if (0 != (m_p_domain->isp_vec[pos])->p_all->pv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->pv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->uv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->uv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->vv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->vv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->iv)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->iv);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->time)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->time);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        (m_p_domain->isp_vec[pos])->p_all->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }
        }
    }

    if (0 != isp_guest.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(), isp_guest.pv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_guest.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(), isp_guest.uv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_guest.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(), isp_guest.vv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_guest.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(), isp_guest.iv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_guest.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(), isp_guest.time);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_guest.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                isp_guest.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(), isp_vip.pv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(), isp_vip.uv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(), isp_vip.vv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(), isp_vip.iv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(), isp_vip.time);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_vip.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(), isp_vip.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.pv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(), isp_all.pv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.uv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(), isp_all.uv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.vv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(), isp_all.vv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.iv)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(), isp_all.iv);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.time)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(), isp_all.time);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != isp_all.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(), isp_all.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }
    return 0;
}

int AggregateRedisHandler::DumpIspData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }
    //int status;
    int size = m_p_domain->isp_vec.size();
    int pos = 0;
/*
    ESAIspItem isp_guest;
    ESAIspItem isp_vip;
    ESAIspItem isp_all;

    memset(&isp_guest, 0x00, sizeof(ESAIspItem));
    memset(&isp_vip, 0x00, sizeof(ESAIspItem));
    memset(&isp_all, 0x00, sizeof(ESAIspItem));
*/
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->isp_vec[pos])
        {
            continue;
        }

        //if ( 0 == ( (m_p_domain->isp_vec[pos])->add_flag & m_self_index ) )
        //{   
        //    (m_p_domain->isp_vec[pos])->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_ISP,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                (m_p_domain->isp_vec[pos])->ispcode);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u", RTA_REDIS_ISP,
                m_p_domain->domain, (m_p_domain->isp_vec[pos])->ispcode);

        //---------------
        if (NULL != (m_p_domain->isp_vec[pos])->p_guest)
        {
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_pv.c_str(), m_buf);
                //isp_guest.pv += (m_p_domain->isp_vec[pos])->p_guest->pv;
                //isp_all.pv += (m_p_domain->isp_vec[pos])->p_guest->pv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_uv.c_str(), m_buf);
                //isp_guest.uv += (m_p_domain->isp_vec[pos])->p_guest->uv;
                //isp_all.uv += (m_p_domain->isp_vec[pos])->p_guest->uv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_vv.c_str(), m_buf);
                //isp_guest.vv += (m_p_domain->isp_vec[pos])->p_guest->vv;
                //isp_all.vv += (m_p_domain->isp_vec[pos])->p_guest->vv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_iv.c_str(), m_buf);
                //isp_guest.iv += (m_p_domain->isp_vec[pos])->p_guest->iv;
                //isp_all.iv += (m_p_domain->isp_vec[pos])->p_guest->iv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_time.c_str(), m_buf);
                //isp_guest.time += (m_p_domain->isp_vec[pos])->p_guest->time;
                //isp_all.time += (m_p_domain->isp_vec[pos])->p_guest->time;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
                //isp_guest.bounce += (m_p_domain->isp_vec[pos])->p_guest->bounce;
                //isp_all.bounce += (m_p_domain->isp_vec[pos])->p_guest->bounce;
            }

        }

        if (NULL != (m_p_domain->isp_vec[pos])->p_vip)
        {
            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_pv.c_str(), m_buf);
                //isp_vip.pv += (m_p_domain->isp_vec[pos])->p_vip->pv;
                //isp_all.pv += (m_p_domain->isp_vec[pos])->p_vip->pv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_uv.c_str(), m_buf);
                //isp_vip.uv += (m_p_domain->isp_vec[pos])->p_vip->uv;
                //isp_all.uv += (m_p_domain->isp_vec[pos])->p_vip->uv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_vv.c_str(), m_buf);
                //isp_vip.vv += (m_p_domain->isp_vec[pos])->p_vip->vv;
                //isp_all.vv += (m_p_domain->isp_vec[pos])->p_vip->vv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_iv.c_str(), m_buf);
                //isp_vip.iv += (m_p_domain->isp_vec[pos])->p_vip->iv;
                //isp_all.iv += (m_p_domain->isp_vec[pos])->p_vip->iv;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_time.c_str(), m_buf);
                //isp_vip.time += (m_p_domain->isp_vec[pos])->p_vip->time;
                //isp_all.time += (m_p_domain->isp_vec[pos])->p_vip->time;
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
                //isp_vip.bounce += (m_p_domain->isp_vec[pos])->p_vip->bounce;
                //isp_all.bounce += (m_p_domain->isp_vec[pos])->p_vip->bounce;
            }
        }

        //---------------
        if (NULL != (m_p_domain->isp_vec[pos])->p_all)
        {
            if (0 != (m_p_domain->isp_vec[pos])->p_all->pv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->pv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_pv.c_str(), m_buf);
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->uv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->uv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_uv.c_str(), m_buf);
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->vv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->vv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_vv.c_str(), m_buf);
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->iv)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->iv);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_iv.c_str(), m_buf);
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->time)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->time);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_time.c_str(), m_buf);
            }

            //---------------
            if (0 != (m_p_domain->isp_vec[pos])->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                        (m_p_domain->isp_vec[pos])->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
/*
    snprintf( m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
        RTA_REDIS_ISP,
        m_p_domain->domain,
        (m_p_domain->isp_vec[pos])->ispcode);

    if (0 != isp_guest.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_pv.c_str(), m_buf);
    }

    //---------------
    if (0 != isp_guest.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_uv.c_str(), m_buf);
    }

    //---------------
    if (0 != isp_guest.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_vv.c_str(), m_buf);
    }

    //---------------
    if (0 != isp_guest.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_iv.c_str(), m_buf);
    }

    //---------------
    if (0 != isp_guest.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_time.c_str(), m_buf);
    }

    //---------------
    if (0 != isp_guest.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_guest.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_bounce.c_str(), m_buf);
    }

    if (0 != isp_vip.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_pv.c_str(),
                m_buf);
    }

    if (0 != isp_vip.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_uv.c_str(),
                m_buf);
    }

    if (0 != isp_vip.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_vv.c_str(),
                m_buf);
    }

    if (0 != isp_vip.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_vip_iv.c_str(),
                m_buf);
    }

    if (0 != isp_vip.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_time.c_str(), m_buf);
    }

    if (0 != isp_vip.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_vip.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_bounce.c_str(), m_buf);
    }

    if (0 != isp_all.pv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.pv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_pv.c_str(),
                m_buf);
    }

    if (0 != isp_all.uv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.uv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_uv.c_str(),
                m_buf);
    }

    if (0 != isp_all.vv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.vv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_vv.c_str(),
                m_buf);
    }

    if (0 != isp_all.iv)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.iv);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key, g_str_all_iv.c_str(),
                m_buf);
    }

    if (0 != isp_all.time)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.time);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_time.c_str(), m_buf);
    }

    if (0 != isp_all.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", isp_all.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_bounce.c_str(), m_buf);
    }
*/
    return 0;
}

int AggregateRedisHandler::DumpEnvData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    int size = m_p_domain->env_vec.size();
    int pos = 0;
    int inner_size = 0;
    int inner_pos = 0;
    ESAEnvBasicVec *p_inner_vec = NULL;

    ESAEnvItem env_guest_sum;
    ESAEnvItem env_vip_sum;
    ESAEnvItem env_all_sum;

    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->env_vec[pos])
        {
            continue;
        }

        memset(&env_guest_sum, 0x00, sizeof(ESAEnvItem));
        memset(&env_vip_sum, 0x00, sizeof(ESAEnvItem));
        memset(&env_all_sum, 0x00, sizeof(ESAEnvItem));

        //if ( 0 == ( (m_p_domain->env_vec[pos])->add_flag & m_self_index ) )
        //{
        //    (m_p_domain->env_vec[pos])->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_ENV_TYPE,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                (m_p_domain->env_vec[pos])->type);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        p_inner_vec = &((m_p_domain->env_vec[pos])->m_env_vec);
        if (NULL == p_inner_vec)
        {
            continue;
        }
        inner_size = p_inner_vec->size();
        for (inner_pos = 0; inner_pos < inner_size; inner_pos++)
        {
            //---------------
            if (NULL != (*p_inner_vec)[inner_pos])
            {
                snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                        RTA_REDIS_ENV, m_p_domain->domain,
                        (m_p_domain->env_vec[pos])->type);
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (*p_inner_vec)[inner_pos]->value);
                WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);

                snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u:%u",
                        RTA_REDIS_ENV, m_p_domain->domain,
                        (m_p_domain->env_vec[pos])->type,
                        (*p_inner_vec)[inner_pos]->value);

                if (NULL != (*p_inner_vec)[inner_pos]->p_guest)
                {
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->pv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->pv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_pv.c_str(), m_buf);
                        env_guest_sum.pv +=
                                (*p_inner_vec)[inner_pos]->p_guest->pv;
                        env_all_sum.pv +=
                                (*p_inner_vec)[inner_pos]->p_guest->pv;
                    }

                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->uv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->uv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_uv.c_str(), m_buf);
                        env_guest_sum.uv +=
                                (*p_inner_vec)[inner_pos]->p_guest->uv;
                        env_all_sum.uv +=
                                (*p_inner_vec)[inner_pos]->p_guest->uv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->vv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->vv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_vv.c_str(), m_buf);
                        env_guest_sum.vv +=
                                (*p_inner_vec)[inner_pos]->p_guest->vv;
                        env_all_sum.vv +=
                                (*p_inner_vec)[inner_pos]->p_guest->vv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->iv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->iv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_iv.c_str(), m_buf);
                        env_guest_sum.iv +=
                                (*p_inner_vec)[inner_pos]->p_guest->iv;
                        env_all_sum.iv +=
                                (*p_inner_vec)[inner_pos]->p_guest->iv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->time)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->time);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_time.c_str(), m_buf);
                        env_guest_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_guest->time;
                        env_all_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_guest->time;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->bounce)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_guest->bounce);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_guest_bounce.c_str(), m_buf);
                        env_guest_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_guest->bounce;
                        env_all_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_guest->bounce;
                    }

                }

                if (NULL != (*p_inner_vec)[inner_pos]->p_vip)
                {
                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->pv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->pv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_pv.c_str(), m_buf);
                        env_vip_sum.pv += (*p_inner_vec)[inner_pos]->p_vip->pv;
                        env_all_sum.pv += (*p_inner_vec)[inner_pos]->p_vip->pv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->uv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->uv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_uv.c_str(), m_buf);
                        env_vip_sum.uv += (*p_inner_vec)[inner_pos]->p_vip->uv;
                        env_all_sum.uv += (*p_inner_vec)[inner_pos]->p_vip->uv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->vv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->vv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_vv.c_str(), m_buf);
                        env_vip_sum.vv += (*p_inner_vec)[inner_pos]->p_vip->vv;
                        env_all_sum.vv += (*p_inner_vec)[inner_pos]->p_vip->vv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->iv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->iv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_iv.c_str(), m_buf);
                        env_vip_sum.iv += (*p_inner_vec)[inner_pos]->p_vip->iv;
                        env_all_sum.iv += (*p_inner_vec)[inner_pos]->p_vip->iv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->time)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->time);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_time.c_str(), m_buf);
                        env_vip_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_vip->time;
                        env_all_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_vip->time;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->bounce)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_vip->bounce);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_vip_bounce.c_str(), m_buf);
                        env_vip_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_vip->bounce;
                        env_all_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_vip->bounce;
                    }
                }

                if (NULL != (*p_inner_vec)[inner_pos]->p_all)
                {
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->pv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->pv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_pv.c_str(), m_buf);
                    }

                    if (0 != (*p_inner_vec)[inner_pos]->p_all->uv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->uv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_uv.c_str(), m_buf);
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->vv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->vv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_vv.c_str(), m_buf);
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->iv)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->iv);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_iv.c_str(), m_buf);
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->time)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->time);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_time.c_str(), m_buf);
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->bounce)
                    {
                        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d",
                                (*p_inner_vec)[inner_pos]->p_all->bounce);
                        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                                g_str_all_bounce.c_str(), m_buf);
                    }
                }
            }
        }
        /*
         snprintf( m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
         RTA_REDIS_ENV,
         m_p_domain->domain,
         (m_p_domain->env_vec[pos])->type);
         WriteToRedisSadd( g_str_sadd.c_str(), m_buf_key, "0");
         */
/*
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u:0", RTA_REDIS_ENV,
                m_p_domain->domain, (m_p_domain->env_vec[pos])->type);

        if (0 != env_guest_sum.pv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.pv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_pv.c_str(), m_buf);
        }

        if (0 != env_guest_sum.uv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.uv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_uv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_guest_sum.vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_guest_sum.iv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.iv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_iv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_guest_sum.time)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.time);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_time.c_str(), m_buf);
        }

        //---------------
        if (0 != env_guest_sum.bounce)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_guest_sum.bounce);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_bounce.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.pv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.pv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_pv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.uv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.uv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_uv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.iv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.iv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_iv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.time)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.time);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_time.c_str(), m_buf);
        }

        //---------------
        if (0 != env_vip_sum.bounce)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_vip_sum.bounce);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_bounce.c_str(), m_buf);
        }
        if (0 != env_all_sum.pv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.pv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_pv.c_str(), m_buf);
        }

        if (0 != env_all_sum.uv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.uv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_uv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_all_sum.vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_all_sum.iv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.iv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_iv.c_str(), m_buf);
        }

        //---------------
        if (0 != env_all_sum.time)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.time);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_time.c_str(), m_buf);
        }

        //---------------
        if (0 != env_all_sum.bounce)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%d", env_all_sum.bounce);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_bounce.c_str(), m_buf);
        }
	*/
    }
    return 0;
}

int AggregateRedisHandler::LogEnvData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }
    int size = m_p_domain->env_vec.size();
    int pos = 0;
    int inner_size = 0;
    int inner_pos = 0;
    char *p_buf = NULL;
    int len = 0;

    ESAEnvItem env_guest_sum;
    ESAEnvItem env_vip_sum;
    ESAEnvItem env_all_sum;

    ESAEnvBasicVec *p_inner_vec = NULL;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->env_vec[pos])
        {
            continue;
        }

        memset(&env_guest_sum, 0x00, sizeof(ESAEnvItem));
        memset(&env_vip_sum, 0x00, sizeof(ESAEnvItem));
        memset(&env_all_sum, 0x00, sizeof(ESAEnvItem));
        
        //if ( 0 == ( (m_p_domain->env_vec[pos])->add_flag & m_self_index ) )
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %u",
                g_str_sadd.c_str(), RTA_REDIS_ENV_TYPE, m_p_domain->domain,
                (m_p_domain->env_vec[pos])->type);
        Log2File(m_buf);
        m_send_count++;
        //}

        p_inner_vec = &((m_p_domain->env_vec[pos])->m_env_vec);
        if (NULL == p_inner_vec)
        {
            continue;
        }

        inner_size = p_inner_vec->size();
        for (inner_pos = 0; inner_pos < inner_size; inner_pos++)
        {
            //---------------
            if (NULL != (*p_inner_vec)[inner_pos])
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u %u",
                        g_str_sadd.c_str(), RTA_REDIS_ENV, m_p_domain->domain,
                        (m_p_domain->env_vec[pos])->type,
                        (*p_inner_vec)[inner_pos]->value);
                Log2File(m_buf);
                m_send_count++;

                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u:%u ",
                        g_str_hincrby.c_str(), RTA_REDIS_ENV,
                        m_p_domain->domain, (m_p_domain->env_vec[pos])->type,
                        (*p_inner_vec)[inner_pos]->value);

                p_buf = m_buf + strlen(m_buf);
                len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

                if (NULL != (*p_inner_vec)[inner_pos]->p_guest)
                {
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->pv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->pv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.pv +=
                                (*p_inner_vec)[inner_pos]->p_guest->pv;
                        env_all_sum.pv +=
                                (*p_inner_vec)[inner_pos]->p_guest->pv;
                    }

                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->uv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->uv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.uv +=
                                (*p_inner_vec)[inner_pos]->p_guest->uv;
                        env_all_sum.uv +=
                                (*p_inner_vec)[inner_pos]->p_guest->uv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->vv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->vv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.vv +=
                                (*p_inner_vec)[inner_pos]->p_guest->vv;
                        env_all_sum.vv +=
                                (*p_inner_vec)[inner_pos]->p_guest->vv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->iv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->iv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.iv +=
                                (*p_inner_vec)[inner_pos]->p_guest->iv;
                        env_all_sum.iv +=
                                (*p_inner_vec)[inner_pos]->p_guest->iv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->time)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->time);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_guest->time;
                        env_all_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_guest->time;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_guest->bounce)
                    {
                        snprintf(p_buf, len, "%s %d",
                                g_str_guest_bounce.c_str(),
                                (*p_inner_vec)[inner_pos]->p_guest->bounce);
                        Log2File(m_buf);
                        m_send_count++;
                        env_guest_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_guest->bounce;
                        env_all_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_guest->bounce;
                    }

                }

                if (NULL != (*p_inner_vec)[inner_pos]->p_vip)
                {
                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->pv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->pv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.pv += (*p_inner_vec)[inner_pos]->p_vip->pv;
                        env_all_sum.pv += (*p_inner_vec)[inner_pos]->p_vip->pv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->uv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->uv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.uv += (*p_inner_vec)[inner_pos]->p_vip->uv;
                        env_all_sum.uv += (*p_inner_vec)[inner_pos]->p_vip->uv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->vv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->vv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.vv += (*p_inner_vec)[inner_pos]->p_vip->vv;
                        env_all_sum.vv += (*p_inner_vec)[inner_pos]->p_vip->vv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->iv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->iv);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.iv += (*p_inner_vec)[inner_pos]->p_vip->iv;
                        env_all_sum.iv += (*p_inner_vec)[inner_pos]->p_vip->iv;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->time)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->time);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_vip->time;
                        env_all_sum.time +=
                                (*p_inner_vec)[inner_pos]->p_vip->time;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_vip->bounce)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                                (*p_inner_vec)[inner_pos]->p_vip->bounce);
                        Log2File(m_buf);
                        m_send_count++;
                        env_vip_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_vip->bounce;
                        env_all_sum.bounce +=
                                (*p_inner_vec)[inner_pos]->p_vip->bounce;
                    }
                }

                if (NULL != (*p_inner_vec)[inner_pos]->p_all)
                {
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->pv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->pv);
                        Log2File(m_buf);
                        m_send_count++;
                    }

                    if (0 != (*p_inner_vec)[inner_pos]->p_all->uv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->uv);
                        Log2File(m_buf);
                        m_send_count++;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->vv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->vv);
                        Log2File(m_buf);
                        m_send_count++;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->iv)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->iv);
                        Log2File(m_buf);
                        m_send_count++;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->time)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->time);
                        Log2File(m_buf);
                        m_send_count++;
                    }

                    //---------------
                    if (0 != (*p_inner_vec)[inner_pos]->p_all->bounce)
                    {
                        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                                (*p_inner_vec)[inner_pos]->p_all->bounce);
                        Log2File(m_buf);
                        m_send_count++;
                    }
                }
            }
        }
        /*
         snprintf( m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u 0",
         g_str_sadd.c_str(),
         RTA_REDIS_ENV,
         m_p_domain->domain,
         (m_p_domain->env_vec[pos])->type);
         Log2File( m_buf );
         m_send_count++;
         */
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u:0 ",
                g_str_hincrby.c_str(), RTA_REDIS_ENV, m_p_domain->domain,
                (m_p_domain->env_vec[pos])->type);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        if (0 != env_guest_sum.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_pv.c_str(),
                    env_guest_sum.pv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_guest_sum.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_uv.c_str(),
                    env_guest_sum.uv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_guest_sum.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                    env_guest_sum.vv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_guest_sum.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_iv.c_str(),
                    env_guest_sum.iv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_guest_sum.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_time.c_str(),
                    env_guest_sum.time);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_guest_sum.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                    env_guest_sum.bounce);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_pv.c_str(), env_vip_sum.pv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_uv.c_str(), env_vip_sum.uv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(), env_vip_sum.vv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_iv.c_str(), env_vip_sum.iv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_time.c_str(),
                    env_vip_sum.time);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_vip_sum.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                    env_vip_sum.bounce);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.pv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_pv.c_str(), env_all_sum.pv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.uv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_uv.c_str(), env_all_sum.uv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(), env_all_sum.vv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.iv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_iv.c_str(), env_all_sum.iv);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.time)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_time.c_str(),
                    env_all_sum.time);
            Log2File(m_buf);
            m_send_count++;
        }

        if (0 != env_all_sum.bounce)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                    env_all_sum.bounce);
            Log2File(m_buf);
            m_send_count++;
        }

    }
    return 0;
}

int AggregateRedisHandler::DumpDMDepthData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    int size = m_p_domain->dm_depth_vec.size();
    int pos = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->dm_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ( (m_p_domain->dm_depth_vec[pos])->add_flag & m_self_index ))     
        //{
        //    (m_p_domain->dm_depth_vec[pos])->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_DM_DEPTH,
                m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                (m_p_domain->dm_depth_vec[pos])->depth);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        //---------------
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                RTA_REDIS_DM_DEPTH, m_p_domain->domain,
                (m_p_domain->dm_depth_vec[pos])->depth);

        if (0 != (m_p_domain->dm_depth_vec[pos])->guest_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->dm_depth_vec[pos])->guest_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->dm_depth_vec[pos])->vip_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->dm_depth_vec[pos])->vip_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->dm_depth_vec[pos])->all_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->dm_depth_vec[pos])->all_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_vv.c_str(), m_buf);
        }
    }
    return 0;
}

int AggregateRedisHandler::LogDMDepthData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }
    int size = m_p_domain->dm_depth_vec.size();
    int pos = 0;
    char *p_buf = NULL;
    int len = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->dm_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ( (m_p_domain->dm_depth_vec[pos])->add_flag & m_self_index ) )    
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "0:%u:%u %u", g_str_sadd.c_str(),
                RTA_REDIS_DM_DEPTH, m_p_domain->domain,
                (m_p_domain->dm_depth_vec[pos])->depth);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf);
        m_send_count++;
        //}
        
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_DM_DEPTH, m_p_domain->domain,
                (m_p_domain->dm_depth_vec[pos])->depth);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);
        //---------------
        if (0 != (m_p_domain->dm_depth_vec[pos])->guest_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                    (m_p_domain->dm_depth_vec[pos])->guest_vv);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->dm_depth_vec[pos])->vip_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                    (m_p_domain->dm_depth_vec[pos])->vip_vv);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->dm_depth_vec[pos])->all_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                    (m_p_domain->dm_depth_vec[pos])->all_vv);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf);
            m_send_count++;
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpPlateDepthData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    int size = m_p_domain->plate_depth_vec.size();
    int pos = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->plate_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ((m_p_domain->plate_depth_vec[pos])->add_flag & m_self_index ))     
        //{
        //    (m_p_domain->plate_depth_vec[pos])->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                RTA_REDIS_PLATE_DEPTH, m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                (m_p_domain->plate_depth_vec[pos])->depth);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        //---------------
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                RTA_REDIS_PLATE_DEPTH, m_p_domain->domain,
                (m_p_domain->plate_depth_vec[pos])->depth);

        if (0 != (m_p_domain->plate_depth_vec[pos])->guest_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->plate_depth_vec[pos])->guest_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->plate_depth_vec[pos])->vip_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->plate_depth_vec[pos])->vip_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->plate_depth_vec[pos])->all_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->plate_depth_vec[pos])->all_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_vv.c_str(), m_buf);
        }
    }
    return 0;
}

int AggregateRedisHandler::LogPlateDepthData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }
    int size = m_p_domain->plate_depth_vec.size();
    int pos = 0;
    char *p_buf = NULL;
    int len = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->plate_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ( (m_p_domain->plate_depth_vec[pos])->add_flag & m_self_index ))     
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "0:%u:%u %u", g_str_sadd.c_str(),
                RTA_REDIS_PLATE_DEPTH, m_p_domain->domain,
                (m_p_domain->plate_depth_vec[pos])->depth);
        Log2File(m_buf);
        m_send_count++;
        //}
        
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_PLATE_DEPTH,
                m_p_domain->domain, (m_p_domain->plate_depth_vec[pos])->depth);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);
        //---------------
        if (0 != (m_p_domain->plate_depth_vec[pos])->guest_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                    (m_p_domain->plate_depth_vec[pos])->guest_vv);
            Log2File(m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->plate_depth_vec[pos])->vip_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                    (m_p_domain->plate_depth_vec[pos])->vip_vv);
            Log2File(m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->plate_depth_vec[pos])->all_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                    (m_p_domain->plate_depth_vec[pos])->all_vv);
            Log2File(m_buf);
            m_send_count++;
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpThreadDepthData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    //int status;
    int size = m_p_domain->thread_depth_vec.size();
    int pos = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->thread_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ((m_p_domain->thread_depth_vec[pos])->add_flag & m_self_index ))     
        //{
        //    (m_p_domain->thread_depth_vec[pos])->add_flag |= m_self_index;
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                RTA_REDIS_THREAD_DEPTH, m_p_domain->domain);
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                (m_p_domain->thread_depth_vec[pos])->depth);
        WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        //}

        //---------------
        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%u",
                RTA_REDIS_THREAD_DEPTH, m_p_domain->domain,
                (m_p_domain->thread_depth_vec[pos])->depth);

        if (0 != (m_p_domain->thread_depth_vec[pos])->guest_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->thread_depth_vec[pos])->guest_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_guest_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->thread_depth_vec[pos])->vip_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->thread_depth_vec[pos])->vip_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_vip_vv.c_str(), m_buf);
        }

        //---------------
        if (0 != (m_p_domain->thread_depth_vec[pos])->all_vv)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                    (m_p_domain->thread_depth_vec[pos])->all_vv);
            WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                    g_str_all_vv.c_str(), m_buf);
        }
    }
    return 0;
}

int AggregateRedisHandler::LogThreadDepthData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }
    int size = m_p_domain->thread_depth_vec.size();
    int pos = 0;
    char *p_buf = NULL;
    int len = 0;
    for (; pos < size; pos++)
    {
        if (NULL == m_p_domain->thread_depth_vec[pos])
        {
            continue;
        }

        //if ( 0 == ((m_p_domain->thread_depth_vec[pos])->add_flag & m_self_index ))     
        //{
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "0:%u:%u %u", g_str_sadd.c_str(),
                RTA_REDIS_THREAD_DEPTH, m_p_domain->domain,
                (m_p_domain->thread_depth_vec[pos])->depth);
        Log2File(m_buf);
        m_send_count++;
        //}
        
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%u ",
                g_str_hincrby.c_str(), RTA_REDIS_THREAD_DEPTH,
                m_p_domain->domain, (m_p_domain->thread_depth_vec[pos])->depth);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);
        //---------------
        if (0 != (m_p_domain->thread_depth_vec[pos])->guest_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_vv.c_str(),
                    (m_p_domain->thread_depth_vec[pos])->guest_vv);
            Log2File(m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->thread_depth_vec[pos])->vip_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_vv.c_str(),
                    (m_p_domain->thread_depth_vec[pos])->vip_vv);
            Log2File(m_buf);
            m_send_count++;
        }

        //---------------
        if (0 != (m_p_domain->thread_depth_vec[pos])->all_vv)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_vv.c_str(),
                    (m_p_domain->thread_depth_vec[pos])->all_vv);
            Log2File(m_buf);
            m_send_count++;
        }
    }
    return 0;
}

int AggregateRedisHandler::DumpLandPageData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESALandPageItem guest_sum;
    ESALandPageItem vip_sum;
    ESALandPageItem all_sum;

    memset(&guest_sum, 0x00, sizeof(ESALandPageItem));
    memset(&vip_sum, 0x00, sizeof(ESALandPageItem));
    memset(&all_sum, 0x00, sizeof(ESALandPageItem));

    //int status;
    ESALandPageMap::iterator it;
    for (it = m_p_domain->land_page_map.begin();
            it != m_p_domain->land_page_map.end(); it++)
    {
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                    RTA_REDIS_LAND_PAGE, m_p_domain->domain);
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%llu", it->first);
            WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        }

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%llu",
                RTA_REDIS_LAND_PAGE, m_p_domain->domain, it->first);

        //---------------
        if (NULL != it->second->p_guest)
        {
            if (0 != (it->second)->p_guest->land)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_guest->land);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_land.c_str(), m_buf);
                guest_sum.land += (it->second)->p_guest->land;
                all_sum.land += (it->second)->p_guest->land;
            }

            //---------------
            if (0 != (it->second)->p_guest->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_guest->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_bounce.c_str(), m_buf);
                guest_sum.bounce += (it->second)->p_guest->bounce;
                all_sum.bounce += (it->second)->p_guest->bounce;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != (it->second)->p_vip->land)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_vip->land);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_land.c_str(), m_buf);
                vip_sum.land += (it->second)->p_vip->land;
                all_sum.land += (it->second)->p_vip->land;
            }

            //---------------
            if (0 != (it->second)->p_vip->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_vip->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_bounce.c_str(), m_buf);
                vip_sum.bounce += (it->second)->p_vip->bounce;
                all_sum.bounce += (it->second)->p_vip->bounce;
            }
        }

        //---------------
        if (NULL != it->second->p_all)
        {
            if (0 != (it->second)->p_all->land)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_all->land);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_land.c_str(), m_buf);
            }

            //---------------
            if (0 != (it->second)->p_all->bounce)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->p_all->bounce);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_bounce.c_str(), m_buf);
            }
        }
    }
    /*
     snprintf( m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
     RTA_REDIS_LAND_PAGE,
     m_p_domain->domain);
     WriteToRedisSadd( g_str_sadd.c_str(), m_buf_key, "0" );
     */
    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:0", RTA_REDIS_LAND_PAGE,
            m_p_domain->domain);

    if (0 != guest_sum.land)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", guest_sum.land);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_land.c_str(), m_buf);
    }

    if (0 != guest_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", guest_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_bounce.c_str(), m_buf);
    }

    if (0 != vip_sum.land)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", vip_sum.land);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_land.c_str(), m_buf);
    }

    if (0 != vip_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", vip_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_bounce.c_str(), m_buf);
    }

    if (0 != all_sum.land)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", all_sum.land);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_land.c_str(), m_buf);
    }

    if (0 != all_sum.bounce)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", all_sum.bounce);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_bounce.c_str(), m_buf);
    }
    return 0;
}

int AggregateRedisHandler::LogLandPageData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;

    ESALandPageItem guest_sum;
    ESALandPageItem vip_sum;
    ESALandPageItem all_sum;

    memset(&guest_sum, 0x00, sizeof(ESALandPageItem));
    memset(&vip_sum, 0x00, sizeof(ESALandPageItem));
    memset(&all_sum, 0x00, sizeof(ESALandPageItem));

    ESALandPageMap::iterator it;
    for (it = m_p_domain->land_page_map.begin();
            it != m_p_domain->land_page_map.end(); it++)
    {
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %llu",
                    g_str_sadd.c_str(), RTA_REDIS_LAND_PAGE, m_p_domain->domain,
                    it->first);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf );
            m_send_count++;
        }

        snprintf(m_buf, sizeof(m_buf) - 1, "%s 0:%u:%u:%llu ",
                g_str_hincrby.c_str(), RTA_REDIS_LAND_PAGE, m_p_domain->domain,
                it->first);
        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        //---------------
        if (NULL != it->second->p_guest)
        {
            if (0 != (it->second)->p_guest->land)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_land.c_str(),
                        (it->second)->p_guest->land);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                guest_sum.land += (it->second)->p_guest->land;
                all_sum.land += (it->second)->p_guest->land;
            }

            //---------------
            if (0 != (it->second)->p_guest->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                        (it->second)->p_guest->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                guest_sum.bounce += (it->second)->p_guest->bounce;
                all_sum.bounce += (it->second)->p_guest->bounce;
            }
        }

        if (NULL != it->second->p_vip)
        {
            //---------------
            if (0 != (it->second)->p_vip->land)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_land.c_str(),
                        (it->second)->p_vip->land);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                vip_sum.land += (it->second)->p_vip->land;
                all_sum.land += (it->second)->p_vip->land;
            }

            //---------------
            if (0 != (it->second)->p_vip->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(),
                        (it->second)->p_vip->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
                vip_sum.bounce += (it->second)->p_vip->bounce;
                all_sum.bounce += (it->second)->p_vip->bounce;
            }
        }

        if (NULL != it->second->p_all)
        {
            //---------------
            if (0 != (it->second)->p_all->land)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_land.c_str(),
                        (it->second)->p_all->land);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }

            //---------------
            if (0 != (it->second)->p_all->bounce)
            {
                snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(),
                        (it->second)->p_all->bounce);
                Log2File(m_buf);
                //INFO_LOG("%s", m_buf );
                m_send_count++;
            }
        }
    }
    /*
     snprintf( m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u 0",
     g_str_sadd.c_str(),
     RTA_REDIS_LAND_PAGE,
     m_p_domain->domain);
     Log2File( m_buf );
     m_send_count++;
     */
    snprintf(m_buf, sizeof(m_buf) - 1, "%s 0:%u:%u:0 ", g_str_hincrby.c_str(),
            RTA_REDIS_LAND_PAGE, m_p_domain->domain);

    p_buf = m_buf + strlen(m_buf);
    len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

    if (0 != guest_sum.land)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_land.c_str(), guest_sum.land);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != guest_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_bounce.c_str(),
                guest_sum.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != vip_sum.land)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_land.c_str(), vip_sum.land);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != vip_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_bounce.c_str(), vip_sum.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != all_sum.land)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_land.c_str(), all_sum.land);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (0 != all_sum.bounce)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_bounce.c_str(), all_sum.bounce);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }
    return 0;
}

int AggregateRedisHandler::LogExitPageData(DomainMap::iterator &dm_it)
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    char *p_buf = NULL;
    int len = 0;

    ESAExitPage exp_sum;
    memset(&exp_sum, 0x00, sizeof(ESAExitPage));

    ESAExitPageMap::iterator it;
    for (it = m_p_domain->exit_page_map.begin();
            it != m_p_domain->exit_page_map.end(); it++)
    {
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u %llu",
                    g_str_sadd.c_str(), RTA_REDIS_EXIT_PAGE, m_p_domain->domain,
                    it->first);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf );
            m_send_count++;
        }

        if (NULL == it->second)
        {
            continue;
        }

        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:%llu ",
                g_str_hincrby.c_str(), RTA_REDIS_EXIT_PAGE, m_p_domain->domain,
                it->first);

        p_buf = m_buf + strlen(m_buf);
        len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);

        //---------------
        if (0 != (it->second)->guest_exp)
        {
            snprintf(p_buf, len, "%s %d", g_str_guest_exp.c_str(),
                    (it->second)->guest_exp);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf );
            m_send_count++;
            exp_sum.guest_exp += (it->second)->guest_exp;
            exp_sum.all_exp += (it->second)->all_exp;
        }

        //---------------
        if (0 != it->second->vip_exp)
        {
            snprintf(p_buf, len, "%s %d", g_str_vip_exp.c_str(),
                    (it->second)->vip_exp);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf );
            m_send_count++;
            exp_sum.vip_exp += (it->second)->vip_exp;
            exp_sum.all_exp += (it->second)->all_exp;
        }

        //---------------
        if (0 != it->second->all_exp)
        {
            snprintf(p_buf, len, "%s %d", g_str_all_exp.c_str(),
                    (it->second)->all_exp);
            Log2File(m_buf);
            //INFO_LOG("%s", m_buf );
            m_send_count++;
        }
    }
    /*
     snprintf( m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u 0",
     g_str_sadd.c_str(),
     RTA_REDIS_EXIT_PAGE,
     m_p_domain->domain);
     Log2File(m_buf);
     m_send_count++;
     */
    snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%s 0:%u:%u:0 ",
            g_str_hincrby.c_str(), RTA_REDIS_EXIT_PAGE, m_p_domain->domain);

    p_buf = m_buf + strlen(m_buf);
    len = ESA_DEFAULT_BUF_LEN_ - strlen(m_buf);
    
    if (exp_sum.guest_exp != 0)
    {
        snprintf(p_buf, len, "%s %d", g_str_guest_exp.c_str(),
                exp_sum.guest_exp);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (exp_sum.vip_exp != 0)
    {
        snprintf(p_buf, len, "%s %d", g_str_vip_exp.c_str(), exp_sum.vip_exp);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    if (exp_sum.all_exp != 0)
    {
        snprintf(p_buf, len, "%s %d", g_str_all_exp.c_str(), exp_sum.all_exp);
        Log2File(m_buf);
        //INFO_LOG("%s", m_buf );
        m_send_count++;
    }

    return 0;
}

int AggregateRedisHandler::DumpExitPageData(DomainMap::iterator &dm_it, bool log_flag )
{
    m_p_domain = dm_it->second;
    if (NULL == m_p_domain)
    {
        return -1;
    }

    ESAExitPage exp_sum;
    memset(&exp_sum, 0x00, sizeof(ESAExitPage));

    ESAExitPageMap::iterator it;
    for (it = m_p_domain->exit_page_map.begin();
            it != m_p_domain->exit_page_map.end(); it++)
    {
        if (it->first != 0 && it->first != 1)
        {
            snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
                    RTA_REDIS_EXIT_PAGE, m_p_domain->domain);
            snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%llu", it->first);
            WriteToRedisSadd(g_str_sadd.c_str(), m_buf_key, m_buf);
        }

        snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:%llu",
                RTA_REDIS_EXIT_PAGE, m_p_domain->domain, it->first);

        if (NULL != it->second)
        {
            //---------------
            if (0 != (it->second)->guest_exp)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->guest_exp);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_guest_exp.c_str(), m_buf);
                exp_sum.guest_exp += (it->second)->guest_exp;
                exp_sum.all_exp += (it->second)->guest_exp;
            }

            //---------------
            if (0 != (it->second)->vip_exp)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->vip_exp);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_vip_exp.c_str(), m_buf);
                exp_sum.vip_exp += (it->second)->vip_exp;
                exp_sum.all_exp += (it->second)->vip_exp;
            }

            //---------------
            if (0 != (it->second)->all_exp)
            {
                snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u",
                        (it->second)->all_exp);
                WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                        g_str_all_exp.c_str(), m_buf);
            }
        }
    }
    /*
     snprintf( m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u",
     RTA_REDIS_EXIT_PAGE,
     m_p_domain->domain);
     WriteToRedisSadd( g_str_sadd.c_str(), m_buf_key, "0");
     */
    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u:0", RTA_REDIS_EXIT_PAGE,
            m_p_domain->domain);

    if (0 != exp_sum.guest_exp)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", exp_sum.guest_exp);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_guest_exp.c_str(), m_buf);
    }

    if (0 != exp_sum.vip_exp)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", exp_sum.vip_exp);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_vip_exp.c_str(), m_buf);
    }

    if (0 != exp_sum.all_exp)
    {
        snprintf(m_buf, ESA_DEFAULT_BUF_LEN_, "%u", exp_sum.all_exp);
        WriteToRedisHash(g_str_hincrby.c_str(), m_buf_key,
                g_str_all_exp.c_str(), m_buf);
    }
    return 0;
}

int AggregateRedisHandler::TryDump()
{
    if (ESA_HEAP_WORKING != m_p_heap_manager->GetStat())
    {
        return 0;
    }

    if (ESA_REDIS_NO_NEED_TO_LOAD_ == m_load_from_redis_flag
            || (ESA_REDIS_LOAD_ON_START_ == m_load_from_redis_flag
                    && ESA_REDIS_LOAD_OVER_ == m_load_from_redis_stat)
            || (ESA_REDIS_LOAD_WHEN_NEEDED_ == m_load_from_redis_flag
                    && ESA_REDIS_LOAD_OVER_ == m_load_from_redis_stat))
    {
        //INFO_LOG("[%d]now load flag is %d, stat is %d", m_self_index, m_load_from_redis_flag, m_load_from_redis_stat ); 
        RunCmd();
        return 0;
    }
    else
    {
        //-- just wait for a while
        return -1;
    }
}

int AggregateRedisHandler::RunCmd()
{
    m_skip_flag = false;

    if (!m_p_redis_conn->IsConnected())
    {
        if (m_redis_conn_time == 0)
        {
            DEBUG_LOG(
                    "[%u]Redis is not connected, try reconnect!", m_self_index);
            m_p_redis_conn->Disconnect();
            m_p_redis_conn->ReConnect();
            m_connecting = true;
            if (m_signal_1_cnt < 1)
            {
                m_p_signal_channel->FireSoftSignal(1, 0);
                m_signal_1_cnt++;
            }
            ++m_redis_conn_time;
            return 0;
        }
        else if (m_redis_conn_time < 5)
        {
            DEBUG_LOG("[%u]Reconnecting!", m_self_index);
            if (m_signal_1_cnt < 1)
            {
                m_p_signal_channel->FireSoftSignal(1, 0);
                m_signal_1_cnt++;
            }
            ++m_redis_conn_time;
            return 0;
        }
        else
        {
            //-- skip this dump 
            //DEBUG_LOG("skip_this time \n");
            //m_p_redis_conn->Disconnect();
            m_connecting = false;
            m_skip_flag = true;
            m_redis_conn_time = 0;
        }
    }

    if (false == m_skip_flag)
    {
        if (false == m_db_selected)
        {
            if (m_redis_conn_time <= 3)
            {
                SelectDB();
                if (m_signal_1_cnt < 1)
                {
                    m_p_signal_channel->FireSoftSignal(1, 0);
                    m_signal_1_cnt++;
                }
                ++m_redis_conn_time;
                return 0;
            }
            else
            {
                m_skip_flag = true;
            }
        }
    }

    m_redis_conn_time = 0;
    if (m_reply_count < m_send_count)
    {
        //-- timer is not expired
        //-- still wait for redis replies

        return 0;
    }

    int cnt = m_p_heap_manager->GetHeapCount();
    int i = m_heap_index;
    m_send_count = 0;
    m_reply_count = 0;
    bool done_flag = false;

    for (i = m_heap_index; i < cnt; i++)
    {
        m_dump_fin = false;
        m_heap_index = i;

        DomainMap *p_map = m_p_heap_manager->m_heap_arr[i]->p_domain_map;
        DomainMap::iterator it;
        if (true == m_b_it_is_begin)
        {
            if (0 == i)
            {
                if (0 != m_p_heap_manager->TestLock())
                {
                    DEBUG_LOG("[%u]test lock, give up!", m_self_index);
                    return 0;
                }

                if (true == m_ready_to_set_date)
                {
                    INFO_LOG("[%u] reset date changed to true", m_self_index);
                    m_date_changed = true;
                    m_ready_to_set_date = false;
                }

                if (true == m_ready_to_set_hour)
                {
                    INFO_LOG("[%u] reset hour changed to true", m_self_index);
                    m_hour_changed = true;
                    m_ready_to_set_hour = false;
                }
            }

            //-- iterator is at the begining of the heap 
            //INFO_LOG("it = begin\n");
            it = p_map->begin();
            //INFO_LOG("[%u]p_map size %d", m_self_index, p_map->size());
            m_domain_it = it;
            int status = m_p_heap_manager->SetLock(i, m_self_index);
            if (-2 == status)
            {
                DEBUG_LOG(
                        "[%u] Another handler speed too slow, wait!!", m_self_index);
                m_timer_id = m_p_timer->Schedule(this, 1000, -1);
                /*
                 if ( m_signal_1_cnt < 1 )
                 {
                 m_p_signal_channel->FireSoftSignal(1, 0);
                 m_signal_1_cnt++;
                 }
                 */
                return 0;
            }
            m_b_it_is_begin = false;
            if (0 == i)
            {
                m_dump_start_time = time(NULL);
            }
        }
        else
        {
            //-- iterator is at the middle position of the heap 
            //INFO_LOG("it = middle\n");
            it = m_domain_it;
        }

        for (; it != p_map->end(); it++)
        {
            m_domain_it = it;
            
            if (0 != (it->second->dump_flag & m_self_index))
            {
/*
                if (true == m_date_changed || true == m_hour_changed)
                {
                    if (false == m_skip_flag)
                    {
                        //DumpDomainHourData(it);
                        //DumpDMRefHourData(it);
                    }
                    else
                    {
                        //LogDomainHourData(it);
                        //LogDMRefHourData(it);
                    }
                }
*/
            }
            else
            {
                bool log_flag = false;
                if (false == m_skip_flag)
                {
                    DumpDomainData(it);
                    DumpPlateData(it);
                    DumpPageData(it);
                    DumpThreadData(it);
                    DumpDMRefData(it);
                    DumpSearchEngineData(it);
                    DumpAreaData(it);
                    DumpIspData(it);
                    DumpEnvData(it);
                    DumpDMDepthData(it);
                    DumpPlateDepthData(it);
                    DumpThreadDepthData(it);
                    DumpLandPageData(it);
                    DumpExitPageData(it);
                }
                else
                {
                    //-- the redis is not avalible now
                    //-- try to log these data into file 

                    LogDomainData(it);
                    LogPlateData(it);
                    LogPageData(it);
                    LogThreadData(it);
                    LogDMRefData(it);
                    LogSearchEngineData(it);
                    LogAreaData(it);
                    LogIspData(it);
                    LogEnvData(it);
                    LogDMDepthData(it);
                    LogPlateDepthData(it);
                    LogThreadDepthData(it);
                    LogLandPageData(it);
                    LogExitPageData(it);
                    m_reply_count = m_send_count;
                }
            }

            //-- to notice other dumper that I am already fin dumping this domain's data 
            //-- maybe more than 1 handlers are trying to dump this domain's data
            it->second->dump_flag = (it->second->dump_flag | m_self_index);

            //-- if dump_flag is == heap_manager's dumper mark, then 
            //-- this domain's data has been dumped by all the handlers
            //-- now we can clear this domain
            if (it->second->dump_flag == m_p_heap_manager->GetDumperMark())
            {
                //-- clear this domain's data, reduce memory consumption 
                if (true == m_date_changed)
                {
                    //ClearDomain( it->second );   
                }
                else
                {
                    ZeroDomain(it->second);
                }
            }

            if (m_send_count >= 500)
            {
                m_dump_all += m_send_count;
                m_domain_it++;
                if (true == m_skip_flag)
                {
                    //-- set signal, in order to enter this function again
                    //-- and leave a little time to consumme socket packets
                    if (m_signal_1_cnt < 1)
                    {
                        m_p_signal_channel->FireSoftSignal(1, 0);
                        m_signal_1_cnt++;
                    }
                }
                else
                {
                    //-- set timer, if redis can not reply enough packets in time, timer will alarm
                    //DEBUG_LOG("set timer\n");
                    m_timer_id = m_p_timer->Schedule(this, 3000, -1);
                }
                return 0;
            }
        }

        if (p_map->end() == it)
        {
            m_p_heap_manager->m_heap_arr[i]->map_locked -= m_self_index;
            if (0 == m_p_heap_manager->m_heap_arr[i]->map_locked
                    && true == m_date_changed)
            {
                INFO_LOG(
                        "[%u] unlock[%d] strip: now lock is %d", m_self_index, i, m_p_heap_manager->m_heap_arr[i]->map_locked);
                m_p_heap_manager->Strip(i);
            }
            if (0 == m_p_heap_manager->m_heap_arr[i]->map_locked)
            {
                done_flag = true;
                m_p_heap_manager->HeapBufToMap(m_p_heap_manager->m_heap_arr[i]);
                INFO_LOG(
                        "Heap[%d] buffer size:%d", i, m_p_heap_manager->m_heap_arr[i]->data_buf.size);
            }
            m_b_it_is_begin = true;
        }
    }

    //-- all domains are dumped
    if (cnt == i)
    {
        m_dump_all += m_send_count;
        m_heap_index = 0;
        m_dump_end_time = time(NULL);
        INFO_LOG(
                "[%u] dump fin, dump count %d, used: %d sec", m_self_index, m_dump_all, m_dump_end_time - m_dump_start_time);
        m_dump_fin = true;
        //m_p_heap_manager->PrintHeapStat();
        
        if (0 == m_last_dump_cnt
                && 0 == m_dump_all && m_load_from_redis_flag == ESA_REDIS_LOAD_WHEN_NEEDED_)
        {
            //-- heap manager is idle 
            INFO_LOG("[%u] set heaps to IDLE", m_self_index);
            m_p_heap_manager->LockAll();
            m_p_heap_manager->SetStat(ESA_HEAP_IDLE);
            m_p_heap_manager->Flush();
            m_load_from_redis_stat = ESA_REDIS_READY_TO_LOAD_;
        }
        m_last_dump_cnt = m_dump_all;
        m_dump_all = 0;

        //-- date is changed, need to change redis DB
        //-- and clean those domains which have no data 
        if (true == m_date_changed)
        {
            m_db_index = m_new_date;
            SelectDB();
            m_date_changed = false;
            m_p_heap_manager->ResetVar();
            INFO_LOG("[%u] reset date changed to false", m_self_index);
        }

        if (true == m_hour_changed)
        {
            m_hour_mark = m_new_hour_mark;
            m_hour_changed = false;
        }
    }
    return 0;
}

void AggregateRedisHandler::Run()
{
    if (ESA_REDIS_NO_NEED_TO_LOAD_ == m_load_from_redis_flag
            || (ESA_REDIS_LOAD_ON_START_ == m_load_from_redis_flag
                    && ESA_REDIS_LOAD_OVER_ == m_load_from_redis_stat)
            || (ESA_REDIS_LOAD_WHEN_NEEDED_ == m_load_from_redis_flag
                    && ESA_REDIS_LOAD_OVER_ == m_load_from_redis_stat))
    {
        if (m_reply_count < m_send_count)
        {
            DEBUG_LOG("timer alram");
            m_timer_id = -1;
            if (m_retry_count < 240)
            {
                m_timer_id = m_p_timer->Schedule(this, 500, -1);
                m_retry_count++;
                return;
            }INFO_LOG(
                    "[%u]send count %d, reply_count %d, ", m_self_index, m_send_count, m_reply_count);
            m_retry_count = 0;
            m_reply_count = m_send_count;
        }

        if (false == m_dump_fin)
        {
            RunCmd();
        }
    }
    else
    {
        //load timer alarmed 
        INFO_LOG("load timer alarm");
        m_p_timer->Cancel(m_timer_id);
        m_timer_id = -1;
        LoadFromRedis();
    }
}

int AggregateRedisHandler::GetDBIndex()
{
    return m_db_index;
}

bool AggregateRedisHandler::IsDBSelected()
{
    return m_db_selected;
}

int AggregateRedisHandler::SelectDB()
{
    int status;
    string cmd = "select";
    status = m_p_redis_conn->AsyncCommand(AggregateRedisSelectDBCallBack, this,
            "%s %d", cmd.c_str(), m_db_index);
    INFO_LOG("## cmd:%s %d", cmd.c_str(), m_db_index);
    INFO_LOG("status is %d", status);
    return status;
}

ChannelService* AggregateRedisHandler::GetChannelService()
{
    return m_p_channel_service;
}

void AggregateRedisHandler::SetSelectDBStatus(bool stat)
{
    m_db_selected = stat;
}

int AggregateRedisHandler::HandleFailedReq()
{
    // add code here
    return 0;
}

void AggregateRedisHandler::OnSoftSignal(int soft_signo, uint64 appendinfo)
{
    int stat = 0;
    if (1 == soft_signo)
    {
        if (m_signal_1_cnt > 0)
        {
            m_signal_1_cnt--;
        }
        stat = RunCmd();
        if (stat != 0)
        {
            INFO_LOG("run cmd failed!\n");
        }
    }
    else if (2 == soft_signo)
    {
        if (m_signal_2_cnt > 0)
        {
            m_signal_2_cnt--;
        }
        LoadFromRedis();
    }
}

void AggregateRedisHandler::AddReplyCount(int i)
{
    m_reply_count += i;
}

void AggregateRedisHandler::SetStartTime(time_t time_sec)
{
    m_dump_start_time = time_sec;
}

void AggregateRedisHandler::SetEndTime(time_t time_sec)
{
    m_dump_end_time = time_sec;
}

time_t AggregateRedisHandler::GetDumpUsedTime()
{
    return (m_dump_end_time - m_dump_start_time);
}

RedisConnection* AggregateRedisHandler::GetRedisConn()
{
    return m_p_redis_conn;
}

int AggregateRedisHandler::GetDumpInterval()
{
    return m_dump_interval;
}

void AggregateRedisHandler::ResetTimer()
{
    if (-1 != m_timer_id)
    {
        //INFO_LOG("Cancel timer\n");
        m_p_timer->Cancel(m_timer_id);
        m_timer_id = -1;
    }
}

void AggregateRedisHandler::Log2File(const char *msg)
{
    if (NULL == m_p_logger || NULL == msg)
    {
        return;
    }
    m_p_logger->datamsg("%s", msg);
}

void AggregateRedisHandler::Log2File(vector<string>& vec_msg)
{
    if (NULL == m_p_logger || 0 == vec_msg.size())
    {
        return;
    }

    char* p_buf = m_buf;
    int len = ESA_DEFAULT_BUF_LEN_;
    int vec_size = vec_msg.size();
    
    snprintf(p_buf, len, "%s", vec_msg[0].c_str());
    len -= vec_msg[0].size();
    for (int i = 1; i < vec_size; i++)
    {
        snprintf(p_buf, len, " %s", vec_msg[i].c_str());
        len -= (vec_msg[i].size() + 1);
    }
    m_p_logger->datamsg("%s", m_buf);
}

void AggregateRedisHandler::WriteToRedisHash(const char* cmd,
        const char* hash_name, const char* hash_feild, const char* hash_value)
{
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_WRITE_);
    m_p_session->cmd.push_back(cmd);
    m_p_session->cmd.push_back(hash_name);
    m_p_session->cmd.push_back(hash_feild);
    m_p_session->cmd.push_back(hash_value);
    m_p_session->p_reply->w_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisWriteCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_self_index, m_p_session->cmd );
}

void AggregateRedisHandler::WriteToRedisSadd(const char* cmd,
        const char* set_name, const char* set_value)
{
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_WRITE_);
    m_p_session->cmd.push_back(cmd);
    m_p_session->cmd.push_back(set_name);
    m_p_session->cmd.push_back(set_value);
    m_p_session->p_reply->w_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisWriteCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_self_index, m_p_session->cmd );
}

void AggregateRedisHandler::ReadFromRedisSmembers(const char* cmd,
        const char* set_name)
{
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_READ_);
    m_p_session->cmd.push_back(cmd);
    m_p_session->cmd.push_back(set_name);
    m_p_session->p_reply->r_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisReadDMCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_p_session->cmd );
}

void AggregateRedisHandler::ReadFromRedisHget(const char* cmd,
        const char* hash_name, vector<string> &vec_field, int type, int flag,
        uint32 ext1, uint32 ext2)
{
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_READ_);
    m_p_session->cmd.push_back(cmd);
    m_p_session->cmd.push_back(hash_name);
    int size = vec_field.size();
    for (int i = 0; i < size; i++)
    {
        m_p_session->cmd.push_back(vec_field[i]);
    }
    m_p_session->p_reply->r_reply.ptr = (void*) this;
    m_p_session->p_reply->r_reply.type = type;
    m_p_session->p_reply->r_reply.flag = flag;
    m_p_session->p_reply->r_reply.ext1 = ext1;
    m_p_session->p_reply->r_reply.ext2 = ext2;
    m_p_redis_conn->AsyncCommand(AggregateRedisReadDMDetailCallback,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_p_session->cmd );
}

void AggregateRedisHandler::CheckIfNeedToLoadFromRedis()
{
    if (ESA_REDIS_NO_NEED_TO_LOAD_ == m_load_from_redis_flag)
    {
        return;
    }

    if (ESA_REDIS_LOAD_ON_START_ == m_load_from_redis_flag
            && ESA_REDIS_LOAD_OVER_ != m_load_from_redis_stat)
    {
        LoadFromRedis();
        return;
    }

    if (ESA_REDIS_LOAD_WHEN_NEEDED_ == m_load_from_redis_flag)
    {
        if (ESA_HEAP_IDLE == m_p_heap_manager->GetStat())
        {
            return;
        }
        else if (ESA_HEAP_WAITING_TO_START == m_p_heap_manager->GetStat()
                && ESA_REDIS_LOAD_OVER_ != m_load_from_redis_stat)
        {
            INFO_LOG("check if need to load, and load");
            LoadFromRedis();
        }
    }
}

int AggregateRedisHandler::GetDMBasicFromRedis(uint32 domain)
{
    snprintf(m_buf_key, ESA_DEFAULT_BUF_LEN_, "0:%u:%u", RTA_REDIS_DM, domain);
    vector<string> vec_field;
    vec_field.push_back(g_str_guest_pv);
    vec_field.push_back(g_str_guest_uv);
    vec_field.push_back(g_str_guest_vv);
    vec_field.push_back(g_str_guest_iv);
    vec_field.push_back(g_str_guest_thread);
    vec_field.push_back(g_str_guest_post);
    vec_field.push_back(g_str_guest_time);
    vec_field.push_back(g_str_guest_bounce);

    ReadFromRedisHget(g_str_hmget.c_str(), m_buf_key, vec_field,
            ESA_SUM_TYPE_DOMIAN_, 0, domain, 0);

    vec_field.clear();
    vec_field.push_back(g_str_vip_pv);
    vec_field.push_back(g_str_vip_uv);
    vec_field.push_back(g_str_vip_vv);
    vec_field.push_back(g_str_vip_iv);
    vec_field.push_back(g_str_vip_thread);
    vec_field.push_back(g_str_vip_post);
    vec_field.push_back(g_str_vip_time);
    vec_field.push_back(g_str_vip_bounce);

    ReadFromRedisHget(g_str_hmget.c_str(), m_buf_key, vec_field,
            ESA_SUM_TYPE_DOMIAN_, 1, domain, 0);
    /*
     vec_field.clear();
     vec_field.push_back( g_str_all_pv );
     vec_field.push_back( g_str_all_uv );
     vec_field.push_back( g_str_all_vv );
     vec_field.push_back( g_str_all_iv );
     vec_field.push_back( g_str_all_thread );
     vec_field.push_back( g_str_all_post  );
     vec_field.push_back( g_str_all_time );
     vec_field.push_back( g_str_all_bounce );

     ReadFromRedisHget( g_str_hmget.c_str(), m_buf_key,
     vec_field, ESA_SUM_TYPE_DOMIAN_, 2,  domain, 0 );
     */
    return 0;
}

int AggregateRedisHandler::LoadDetailFromRedis()
{
/*
    if (m_reply_count < m_send_count)
    {
        return 0;
    }
    //INFO_LOG("reply:%d, send;%d, m_domain_index:%d", m_reply_count, m_send_count, m_domain_index );
    m_reply_count = 0;
    m_send_count = 0;
    int size = m_vec_domain.size();
    if (0 >= size)
    {
        
        m_load_from_redis_stat = ESA_REDIS_LOADING_DOMAIN_BASIC_OK_;
        m_domain_index = 0;
        return 0;
    }

    uint32 domain;
    if (0 != m_domain_index)
    {
        if (m_domain_index < size)
        {
            m_domain_index++;
        }
        else
        {
            m_load_from_redis_stat = ESA_REDIS_LOADING_DOMAIN_BASIC_OK_;
            m_domain_index = 0;
            return 0;
        }
    }

    for (; m_domain_index < size; m_domain_index++)
    {
        if (m_vec_domain[m_domain_index].size() <= 0)
        {
            continue;
        }
        domain = (uint32) (atoi(m_vec_domain[m_domain_index].c_str()));
        GetDMBasicFromRedis(domain);

        if (m_send_count >= 1000)
        {
            break;
        }
    }
*/
    return 0;
}

void AggregateRedisHandler::LoadFromRedis()
{
/*
    switch (m_load_from_redis_stat)
    {
        case ESA_REDIS_READY_TO_LOAD_:
            if (!m_p_redis_conn->IsConnected())
            {
                m_p_redis_conn->Disconnect();
                m_p_redis_conn->ReConnect();
                m_timer_id = m_p_timer->Schedule(this, 500, -1);
                break;
            }

            if (m_db_selected != true)
            {
                SelectDB();
                m_timer_id = m_p_timer->Schedule(this, 500, -1);
                break;
            }

            m_vec_domain.clear();
            m_load_from_redis_stat = ESA_REDIS_LOADING_DOMAINS_;
            ReadFromRedisSmembers(g_str_smembers.c_str(), g_str_key_dm.c_str());
            m_timer_id = m_p_timer->Schedule(this, 5000, -1);
            break;

        case ESA_REDIS_LOADING_DOMAINS_:
            if (m_signal_2_cnt < 1)
            {
                m_p_signal_channel->FireSoftSignal(2, 0);
                m_signal_2_cnt++;
            }
            break;

        case ESA_REDIS_LOADING_DOMAINS_OK_:
            LoadDetailFromRedis();
            if (m_signal_2_cnt < 1)
            {
                //INFO_LOG("[%u] loading domains ok", m_self_index );
                m_p_signal_channel->FireSoftSignal(2, 0);
                m_signal_2_cnt++;
            }
            break;

        case ESA_REDIS_LOADING_DOMAIN_BASIC_:
            LoadDetailFromRedis();
            if (m_signal_2_cnt < 1)
            {
                //INFO_LOG("[%u] loading domain's detail", m_self_index );
                m_p_signal_channel->FireSoftSignal(2, 0);
                m_signal_2_cnt++;
            }
            break;

        case ESA_REDIS_LOADING_DOMAIN_BASIC_OK_:
            m_load_from_redis_stat = ESA_REDIS_LOADING_DOMAIN_REF_;
            m_domain_index = 0;
            m_reply_count = 0;
            m_send_count = 0;
            if (m_signal_2_cnt < 1)
            {
                //INFO_LOG("[%u] loading domain's detail fin", m_self_index );
                m_p_signal_channel->FireSoftSignal(2, 0);
                m_signal_2_cnt++;
            }
            break;

        case ESA_REDIS_LOADING_DOMAIN_REF_:
            LoadDMRefFromRedis();
            if (m_load_from_redis_stat == ESA_LOADING_DATA_OVER_)
            {
                m_load_from_redis_stat = ESA_REDIS_LOADING_DOMAIN_REF_OK_;
            }

            if (m_signal_2_cnt < 1)
            {
                m_p_signal_channel->FireSoftSignal(2, 0);
                m_signal_2_cnt++;
            }
            break;

        case ESA_REDIS_LOADING_DOMAIN_REF_OK_:
            m_load_from_redis_stat = ESA_REDIS_LOAD_OVER_;
            //-- no break here
        case ESA_REDIS_LOAD_OVER_:
            if (m_p_heap_manager->GetStat() == ESA_HEAP_WAITING_TO_START)
            {
                m_p_heap_manager->SetStat(ESA_HEAP_WORKING);
            }
            INFO_LOG(
                    "[%u] loading from redis over, domain count: %u", m_self_index, m_vec_domain.size());
            INFO_LOG("[%u] unlock heaps", m_self_index);
            m_vec_domain.clear();
            m_domain_index = 0;
            m_p_heap_manager->UnLockAll();
            break;
        default:
            break;
    }
*/
    m_load_from_redis_stat = ESA_REDIS_LOAD_OVER_;
    if ( m_p_heap_manager->GetStat() == ESA_HEAP_WAITING_TO_START )
    {
        m_p_heap_manager->SetStat(ESA_HEAP_WORKING);
    }
    //INFO_LOG("[%u] loading from redis over, domain count: %u", m_self_index, m_vec_domain.size());
    INFO_LOG("[%u] unlock heaps", m_self_index );
    m_vec_domain.clear();
    m_domain_index = 0;
    m_p_heap_manager->UnLockAll();
    return;
}

void AggregateRedisHandler::AddDomainToVec(const char* dm)
{
    if (NULL == dm)
    {
        return;
    }
    uint32 int_dm = (uint32) atoi(dm);
    uint32 seed = int_dm % 1000;
    if (m_hash_result != seed % m_hash)
    {
        //INFO_LOG("hash:%u, index: %u, dm %s is not my guest", m_hash, m_hash_result, dm);
        return;
    }
    m_vec_domain.push_back(dm);
}

void AggregateRedisHandler::SetRedisLoadStatus(int flag)
{
    m_load_from_redis_stat = flag;
}

int AggregateRedisHandler::InsertSumItemToHeap(uint32 data_type, uint32 domain,
        void *sum_data)
{
    if (NULL == sum_data)
    {
        return -1;
    }

    m_p_heap_manager->PutSumData(data_type, domain, sum_data);
    /*
     switch( data_type )
     {
     case( rta::common::RTA_REDIS_DM):
     InsertDMSumItemToHeap( data_type, domain, (ESADomainItem*)sum_data );
     break;
     default:
     INFO_LOG("unknown");
     break;
     }
     */
    return 0;
}

int AggregateRedisHandler::InsertDMSumItemToHeap(uint32 data_type,
        uint32 domain, ESADomainItem *p_sum_data)
{
    if (NULL == p_sum_data)
    {
        return -1;
    }

    if (0 != p_sum_data->isvip && 1 != p_sum_data->isvip
            && 2 != p_sum_data->isvip)
    {
        return -2;
    }
    m_p_heap_manager->PutSumData(data_type, domain, p_sum_data);
    return 0;
}

void AggregateRedisHandler::SetHour(const char* mark)
{
    if (NULL == mark)
    {
        return;
    }INFO_LOG("Hour mark: %s", mark);
    m_new_hour_mark = mark;
    m_ready_to_set_hour = true;
    /*
     m_hour_changed = true;
     */
}

void AggregateRedisHandler::SetDate(int date)
{
    if (date <= 0 || date > 31 || date == m_db_index)
    {
        return;
    }INFO_LOG("Date mark: %d", date);
    m_new_date = date;
    m_ready_to_set_date = true;
    /*
     m_date_changed = true;
     */
}

void AggregateRedisHandler::CheckStat()
{
    if (m_load_from_redis_flag != ESA_REDIS_NO_NEED_TO_LOAD_
            && m_p_heap_manager->GetStat() == ESA_HEAP_WAITING_TO_START)
    {
        INFO_LOG("Check stat and load data from redis");
        LoadFromRedis();
    }
}

void AggregateRedisHandler::AddTmpStrToVec(const char* tmp_str)
{
    if (NULL == tmp_str)
    {
        return;
    }
    m_vec_tmp.push_back(tmp_str);
}

void AggregateRedisHandler::ReadDMRefUrlFromRedisSmembers(const char* cmd,
        const char* set_name)
{
    m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_READ_);
    m_p_session->cmd.push_back(cmd);
    m_p_session->cmd.push_back(set_name);
    m_p_session->p_reply->r_reply.ptr = (void*) this;
    m_p_redis_conn->AsyncCommand(AggregateRedisReadDMRefUrlCallBack,
            m_p_session->p_reply, m_p_session->cmd);
    m_send_count++;
    //print_vector( m_p_session->cmd );
}

int AggregateRedisHandler::LoadDMRefDataFromRedis(uint32 domain,
        vector<string> &vec_url)
{
/*
    if (m_reply_count < m_send_count)
    {
        return 0;
    }

    //INFO_LOG("reply:%d, send;%d, m_domain_index:%d", m_reply_count, m_send_count, m_domain_index );
    m_reply_count = 0;
    m_send_count = 0;
    int size = m_vec_tmp.size();
    if (0 >= size)
    {
        m_load_from_redis_sub_stat = ESA_LOADING_DATA_OK_;
        m_sub_index = 0;
        return 2;
    }

    if (0 != m_sub_index)
    {
        if (m_sub_index < size - 1)
        {
            m_sub_index++;
        }
        else
        {
            //INFO_LOG("loading data ok--");
            m_load_from_redis_sub_stat = ESA_LOADING_DATA_OK_;
            m_sub_index = 0;
            return 0;
        }
    }

    string str_item;
    snprintf(m_buf_key, sizeof(m_buf_key) - 1, "0:%u:%u:", RTA_REDIS_DM_REF,
            domain);
    char *p_tmp = m_buf_key + strlen(m_buf_key);
    int buf_size = sizeof(m_buf_key) - strlen(m_buf_key) - 1;

    for (; m_sub_index < size; m_sub_index++)
    {
        if (m_vec_tmp[m_sub_index].size() <= 0)
        {
            continue;
        }

        str_item = m_vec_tmp[m_sub_index];
        m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_READ_);
        m_p_session->cmd.push_back(g_str_hmget);
        snprintf(p_tmp, buf_size, "%s", m_vec_tmp[m_sub_index].c_str());
        m_p_session->cmd.push_back(m_buf_key);

        m_p_session->cmd.push_back(g_str_guest_pv);
        m_p_session->cmd.push_back(g_str_guest_uv);
        m_p_session->cmd.push_back(g_str_guest_vv);
        m_p_session->cmd.push_back(g_str_guest_iv);
        m_p_session->cmd.push_back(g_str_guest_time);
        m_p_session->cmd.push_back(g_str_guest_bounce);

        m_p_session->p_reply->r_reply.ptr = (void*) this;
        m_p_session->p_reply->r_reply.type = ESA_SUM_TYPE_DOMIAN_REF_;
        m_p_session->p_reply->r_reply.flag = 0;
        m_p_session->p_reply->r_reply.ext1 = domain;
        m_p_session->p_reply->r_reply.ext2 = strtoull(
                m_vec_tmp[m_sub_index].c_str(), NULL, 10);

        m_p_redis_conn->AsyncCommand(AggregateRedisReadDMRefDataCallBack,
                m_p_session->p_reply, m_p_session->cmd);
        m_send_count++;

        m_p_session = m_session_manager.GetNewSession(ESA_SESSION_TYPE_READ_);
        m_p_session->cmd.push_back(g_str_hmget);
        m_p_session->cmd.push_back(m_buf_key);

        m_p_session->cmd.push_back(g_str_vip_pv);
        m_p_session->cmd.push_back(g_str_vip_uv);
        m_p_session->cmd.push_back(g_str_vip_vv);
        m_p_session->cmd.push_back(g_str_vip_iv);
        m_p_session->cmd.push_back(g_str_vip_time);
        m_p_session->cmd.push_back(g_str_vip_bounce);

        m_p_session->p_reply->r_reply.ptr = (void*) this;
        m_p_session->p_reply->r_reply.type = ESA_SUM_TYPE_DOMIAN_REF_;
        m_p_session->p_reply->r_reply.flag = 1;
        m_p_session->p_reply->r_reply.ext1 = domain;
        m_p_session->p_reply->r_reply.ext2 = strtoull(
                m_vec_tmp[m_sub_index].c_str(), NULL, 10);

        m_p_redis_conn->AsyncCommand(AggregateRedisReadDMRefDataCallBack,
                m_p_session->p_reply, m_p_session->cmd);
        m_send_count++;

        if (m_send_count >= 1000)
        {
            break;
        }
    }
*/
    return 0;
}

int AggregateRedisHandler::LoadDMRefFromRedis()
{
/*
    switch (m_load_from_redis_sub_stat)
    {
        case ESA_READY_TO_LOAD_KEY_:
            if (m_domain_index < m_vec_domain.size())
            {
                snprintf(m_buf, sizeof(m_buf) - 1, "0:%u:%s", RTA_REDIS_DM_REF,
                        m_vec_domain[m_domain_index].c_str());
                m_vec_tmp.clear();
                ReadDMRefUrlFromRedisSmembers(g_str_smembers.c_str(), m_buf);
                m_load_from_redis_sub_stat = ESA_LOADING_KEY_;
            }
            else
            {
                m_load_from_redis_sub_stat = ESA_LOADING_DATA_OVER_;
            }
            break;

        case ESA_LOADING_KEY_:
            if (m_reply_count < m_send_count)
            {
                break;
            }
            m_load_from_redis_sub_stat = ESA_READY_TO_LOAD_DATA_;
            m_sub_index = 0;
            m_reply_count = 0;
            m_send_count = 0;
            LoadDMRefDataFromRedis(
                    (uint32) atoi(m_vec_domain[m_domain_index].c_str()),
                    m_vec_tmp);
            break;

        case ESA_READY_TO_LOAD_DATA_:
            LoadDMRefDataFromRedis(
                    (uint32) atoi(m_vec_domain[m_domain_index].c_str()),
                    m_vec_tmp);
            break;

        case ESA_LOADING_DATA_OK_:
            m_load_from_redis_sub_stat = ESA_READY_TO_LOAD_KEY_;
            m_domain_index++;
            break;

        case ESA_LOADING_DATA_OVER_:
            m_sub_index = 0;
            m_domain_index = 0;
            m_vec_tmp.clear();
            m_load_from_redis_stat = ESA_LOADING_DATA_OVER_;
            break;

        default:
            //error
            break;
    }
*/
    return 0;
}

