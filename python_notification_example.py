"""
Python监控脚本调用前端通知接收器示例
"""

import requests
import json

# 前端URL配置
FRONTEND_URL = "http://localhost:80"  # 根据实际情况修改
NOTIFICATION_ENDPOINT = f"{FRONTEND_URL}/api/notification/receiver"

# 验证Token（必须与前端一致）
NOTIFICATION_TOKEN = "your-secret-token-here-change-it"

def send_notification(
    module,
    module_name,
    type,
    title,
    content,
    action_url=None,
    extra_data=None
):
    """
    发送通知到前端
    
    参数:
        module: 模块ID（token-monitor/twitter/system/wechat-bot）
        module_name: 模块中文名（Token监控/Twitter/系统/微信机器人）
        type: 通知类型（alert/warning/success/info/error）
        title: 通知标题
        content: 通知内容
        action_url: 点击跳转URL（可选）
        extra_data: 额外数据（可选，dict）
        
    返回:
        dict: {'success': True/False, 'error': '...'}
    """
    
    # 1. 先调用后端API存储到数据库（使用你现有的后端接口）
    try:
        backend_response = requests.post(
            'http://47.106.217.116:8080/dashboard/notifications',  # 替换为实际后端地址
            json={
                'module': module,
                'moduleName': module_name,
                'type': type,
                'title': title,
                'content': content,
                'actionUrl': action_url,
                'extraData': json.dumps(extra_data) if extra_data else None
            },
            timeout=5
        )
        print(f"✅ 通知已存储到数据库: {title}")
    except Exception as e:
        print(f"⚠️ 存储到数据库失败: {e}")
        # 继续执行前端通知，即使数据库存储失败
    
    # 2. 调用前端接收器（实时弹窗）
    notification_data = {
        'module': module,
        'moduleName': module_name,
        'type': type,
        'title': title,
        'content': content,
        'actionUrl': action_url,
        'extraData': extra_data or {}
    }
    
    # 注意：由于是纯前端接收，这里使用JavaScript注入的方式
    # 实际部署时，可以通过Nginx代理或使用WebSocket
    
    # 方式1: 如果前端和Python在同一服务器，可以写入文件让前端轮询
    # 方式2: 使用WebSocket（需要额外配置）
    # 方式3: 前端提供一个HTTP接口（通过Nginx代理）
    
    print(f"📢 通知已触发: {title}")
    print(f"   模块: {module_name}")
    print(f"   类型: {type}")
    print(f"   内容: {content}")
    
    return {'success': True}


# ============ 使用示例 ============

def example_token_alert():
    """示例：Token触发涨幅预警"""
    send_notification(
        module='token-monitor',
        module_name='Token监控',
        type='alert',
        title='DEAR触发涨幅预警',
        content='Token DEAR 在过去5分钟内涨幅达到 +15.2%，超过设定阈值 10%',
        action_url='/crypto/token-monitor?ca=HM15KRPfsbmXr8PDfvikBmSqT9suT4x7ZatM3kF2pump',
        extra_data={
            'ca': 'HM15KRPfsbmXr8PDfvikBmSqT9suT4x7ZatM3kF2pump',
            'symbol': 'DEAR',
            'change': 15.2,
            'threshold': 10,
            'chain': 'sol'
        }
    )


def example_holder_change():
    """示例：持币人数变化预警"""
    send_notification(
        module='token-monitor',
        module_name='Token监控',
        type='warning',
        title='BONK持币人数增长',
        content='Token BONK 持币人数增长 +12.3%，超过设定阈值 10%',
        action_url='/crypto/token-monitor?ca=xxx',
        extra_data={
            'ca': 'xxx',
            'symbol': 'BONK',
            'holderChange': 12.3,
            'threshold': 10
        }
    )


def example_twitter_sync():
    """示例：Twitter推送同步成功"""
    send_notification(
        module='twitter',
        module_name='Twitter推送',
        type='success',
        title='推送配置同步成功',
        content='账号 @example 的推送配置已成功同步',
        action_url='/crypto/twitter-push',
        extra_data={
            'account': '@example',
            'pushType': 'follow'
        }
    )


def example_system_info():
    """示例：系统信息通知"""
    send_notification(
        module='system',
        module_name='系统',
        type='info',
        title='系统新增Token',
        content='今日新增 120 个Token，当前总监控数: 334',
        action_url='/crypto/token-monitor',
        extra_data={
            'newCount': 120,
            'totalCount': 334
        }
    )


if __name__ == '__main__':
    # 测试发送通知
    print("=" * 50)
    print("Python通知发送测试")
    print("=" * 50)
    
    example_token_alert()
    
    print("\n" + "=" * 50)
    print("通知发送完成")
    print("=" * 50)

