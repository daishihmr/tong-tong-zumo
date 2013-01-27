tm.net = tm.net || {};
tm.net.event = tm.net.event || {};

(function() {

    tm.net.WebSocket = tm.createClass({
        superClass: tm.event.EventDispatcher,
        socket: null,
        reconnectOnClose: false,
        init: function(url, reconnectOnClose) {
            this.superInit();
            this.url = url;
            this.reconnectOnClose = reconnectOnClose;
            this._connect();
        },
        _connect: function() {
            this.socket = new WebSocket(this.url);

            var self = this;
            this.socket.onopen = function() {
                console.log("open");
                self.dispatchEvent(tm.net.event.Open());
            };
            this.socket.onmessage = function(e) {
                // console.log("message", e.data);
                self.dispatchEvent(tm.net.event.Message(e.data));
            };
            this.socket.onclose = function() {
                console.log("close");
                self.dispatchEvent(tm.net.event.Close());
                self.close();
            };
        },
        send: function(message) {
            if (this.socket !== null && this.socket.readyState === WebSocket.OPEN) {
                // console.log("send");
                this.socket.send(message);
            }
        },
        close: function() {
            if (this.socket !== null) {
                this.socket.close();
            }
            this.socket = null;
        },
    });

    tm.net.event.WebSocketEvent = tm.createClass({        
        superClass: tm.event.Event,    
        init: function(type) {
            this.superInit(type);
        }
    });

    tm.net.event.Open = tm.createClass({
        superClass: tm.net.event.WebSocketEvent,
        init: function() {
            this.superInit("open");
        }
    });

    tm.net.event.Message = tm.createClass({
        superClass: tm.net.event.WebSocketEvent,
        message: null,
        init: function(message) {
            this.superInit("message");
            this.message = message;
        }
    });

    tm.net.event.Close = tm.createClass({
        superClass: tm.net.event.WebSocketEvent,
        init: function() {
            this.superInit("close");
        }
    });

})();
