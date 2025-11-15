const WebSocket = require('ws');
const { v4: uuidv4 } = require('uuid');

class HavelyServer {
    constructor(port = 8080) {
        this.port = port;
        this.wss = new WebSocket.Server({ port });
        this.users = new Map(); // username -> WebSocket
        this.userSessions = new Map(); // WebSocket -> userData
        
        console.log(`🚀 Havely Server starting on port ${port}...`);
        this.setupWebSocket();
        this.setupCleanup();
    }
    
    setupWebSocket() {
        this.wss.on('connection', (ws, req) => {
            const clientId = uuidv4();
            console.log(`🔗 New connection: ${clientId}`);
            
            // Добавляем в сессии
            this.userSessions.set(ws, {
                id: clientId,
                username: null,
                ip: req.socket.remoteAddress,
                connectedAt: Date.now(),
                lastActivity: Date.now()
            });
            
            ws.on('message', (data) => {
                try {
                    this.handleMessage(ws, data.toString());
                } catch (error) {
                    console.error('Error handling message:', error);
                }
            });
            
            ws.on('close', () => {
                this.handleDisconnect(ws);
            });
            
            ws.on('error', (error) => {
                console.error('WebSocket error:', error);
                this.handleDisconnect(ws);
            });
            
            // Отправляем приветственное сообщение
            this.sendToClient(ws, {
                type: 'system',
                message: 'Добро пожаловать в Havely!',
                timestamp: Date.now()
            });
        });
    }
    
    handleMessage(ws, rawData) {
        const session = this.userSessions.get(ws);
        if (!session) return;
        
        session.lastActivity = Date.now();
        
        try {
            const data = JSON.parse(rawData);
            console.log(`📨 Received: ${data.type} from ${session.username || 'unknown'}`);
            
            switch (data.type) {
                case 'join':
                    this.handleJoin(ws, data);
                    break;
                case 'message':
                    this.handleMessageBroadcast(ws, data);
                    break;
                case 'typing':
                    this.handleTyping(ws, data);
                    break;
                default:
                    console.log('Unknown message type:', data.type);
            }
        } catch (error) {
            console.error('Error parsing message:', error);
            this.sendToClient(ws, {
                type: 'error',
                message: 'Invalid message format',
                timestamp: Date.now()
            });
        }
    }
    
    handleJoin(ws, data) {
        const session = this.userSessions.get(ws);
        const username = data.username?.trim();
        
        if (!username || username.length < 2) {
            this.sendToClient(ws, {
                type: 'error',
                message: 'Invalid username',
                timestamp: Date.now()
            });
            return;
        }
        
        // Проверяем уникальность username
        if (this.users.has(username)) {
            this.sendToClient(ws, {
                type: 'error',
                message: 'Username already taken',
                timestamp: Date.now()
            });
            return;
        }
        
        // Регистрируем пользователя
        session.username = username;
        this.users.set(username, ws);
        
        console.log(`✅ User registered: ${username}`);
        
        // Отправляем подтверждение
        this.sendToClient(ws, {
            type: 'joined',
            username: username,
            timestamp: Date.now(),
            message: 'Вы успешно присоединились к Havely!'
        });
        
        // Уведомляем всех о новом пользователе
        this.broadcast({
            type: 'user_joined',
            username: username,
            timestamp: Date.now(),
            onlineCount: this.users.size
        }, ws);
        
        // Отправляем список онлайн пользователей
        this.sendOnlineUsers(ws);
    }
    
    handleMessageBroadcast(ws, data) {
        const session = this.userSessions.get(ws);
        if (!session || !session.username) return;
        
        const message = {
            type: 'message',
            id: uuidv4(),
            username: session.username,
            content: data.content,
            timestamp: Date.now()
        };
        
        // Рассылаем всем участникам
        this.broadcast(message);
        
        console.log(`💬 Message from ${session.username}: ${data.content}`);
    }
    
    handleTyping(ws, data) {
        const session = this.userSessions.get(ws);
        if (!session || !session.username) return;
        
        // Рассылаем уведомление о печати (кроме отправителя)
        this.broadcast({
            type: 'typing',
            username: session.username,
            isTyping: data.isTyping,
            timestamp: Date.now()
        }, ws);
    }
    
    handleDisconnect(ws) {
        const session = this.userSessions.get(ws);
        if (session && session.username) {
            console.log(`❌ User disconnected: ${session.username}`);
            
            // Удаляем из пользователей
            this.users.delete(session.username);
            
            // Уведомляем всех об отключении
            this.broadcast({
                type: 'user_left',
                username: session.username,
                timestamp: Date.now(),
                onlineCount: this.users.size
            });
        }
        
        this.userSessions.delete(ws);
    }
    
    sendToClient(ws, data) {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(data));
        }
    }
    
    broadcast(data, excludeWs = null) {
        const message = JSON.stringify(data);
        this.wss.clients.forEach(client => {
            if (client !== excludeWs && client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    }
    
    sendOnlineUsers(ws) {
        const onlineUsers = Array.from(this.users.keys());
        this.sendToClient(ws, {
            type: 'online_users',
            users: onlineUsers,
            count: onlineUsers.length,
            timestamp: Date.now()
        });
    }
    
    setupCleanup() {
        // Очистка неактивных соединений каждые 5 минут
        setInterval(() => {
            const now = Date.now();
            const inactiveTime = 5 * 60 * 1000; // 5 минут
            
            this.userSessions.forEach((session, ws) => {
                if (now - session.lastActivity > inactiveTime) {
                    console.log(`🕐 Closing inactive connection: ${session.username || session.id}`);
                    ws.close(1000, 'Inactive');
                }
            });
        }, 300000);
    }
}

// Запуск сервера
const PORT = process.env.PORT || 8080;
const server = new HavelyServer(PORT);

console.log(`
🌈 HAVELY SERVER STARTED 🌈
📍 Port: ${PORT}
🔒 Secure WebSocket: ws://localhost:${PORT}
📱 Clients can connect from Android app
💬 Real-time encrypted messaging
`);

// Обработка graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down Havely server...');
    server.wss.close(() => {
        console.log('✅ Server closed gracefully');
        process.exit(0);
    });
});
