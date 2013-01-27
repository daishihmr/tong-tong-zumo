var URL = "ws://" + location.host + "/ws/init";

var app;
var mainScene;
var message;
var fighters = [];

var BULLET_SPEED = 12;
var FIGHTER_SPEED = 12;
var RING_RADIUS = 220;
var BULLET_WEIGHT = 1.5;
var MAX_HP = 480;
var DAMAGE_PAR_FRAME = 5;

tm.preload(function() {
    app = tm.app.CanvasApp("#world");
    app.resize(512, 512);
    app.fitWindow();
    app.replaceScene(tm.app.LoadingScene());
    app.battling = false;
    app.run();

    tm.sound.WebAudioManager.add("shot", "tm2_gun000.wav");
    tm.sound.WebAudioManager.add("hit", "tm2_bom005.wav");
    tm.graphics.TextureManager.add("texture", "texture0.png");
});

tm.main(function() {
    var ws = tm.net.WebSocket(URL);
    ws.addEventListener("message", function(e) {
        var data = JSON.parse(e.message);
        if (data.message === "connected") {
            message.text = "相手と接続しました";
            app.pidx = data.youare|0; // 0 or 1
            app.battling = true;
            start();
        } else if (data.message === "waiting") {
            message.text = "待機中…";
        } else if (data.message === "left") {
            message.text = "相手がいなくなりました";
            app.battling = false;
        } else if (data.message) {
            message.text = data.message;
        }

        if (data.inputs) {
            fighters[0].input(data.inputs[0]);
            fighters[1].input(data.inputs[1]);
        }
    });
    ws.addEventListener("close", function() {
        message.text = "接続が切れました";
        app.battling = false;
    });

    mainScene = tm.app.Scene();
    var background = Background(app);
    mainScene.addChild(background);

    message = tm.app.Label("待機中…");
    message.y = app.height;
    message.width = 320;
    message.setBaseline("bottom");
    mainScene.addChild(message);

    mainScene.update = function(app) {
        if (!app.battling) {
            return;
        }

        var k = app.keyboard;
        var p = app.pointing;
        var input = {
            z: k.getKey("z"),
            x: k.getKey("x"),
            pointing: p.getPointing(),
            pointingStart: p.getPointingStart(),
            pointingEnd: p.getPointingEnd(),
            pointingX: p.x,
            pointingY: p.y,
        };

        ws.send(JSON.stringify(input));
    };

    app.replaceScene(mainScene);
});

var start = function() {
    var bgm = tm.sound.Sound("nc790.mp3");
    setTimeout(function() {
        if (bgm.loaded) {
            bgm.loop = true;
            bgm.play();
        } else {
            setTimeout(arguments.callee, 100);
        }
    }, 100);

    var ip = [
        {
            x: app.width/2-100, y: app.height / 2
        },
        {
            x: app.width/2+100, y: app.height / 2
        },
    ];
    var p = app.pidx;
    var player = fighters[app.pidx] = Fighter(ip[p].x, ip[p].y, "blue", p);
    var enemy = fighters[rv(p)] = Fighter(ip[rv(p)].x, ip[rv(p)].y, "red", rv(p));
    mainScene.addChild(player);
    mainScene.addChild(enemy);

    mainScene.addChild(HpBar(player, 15, "rgba(100, 100, 255, 0.8)"));
    mainScene.addChild(HpBar(enemy,  25, "rgba(255, 100, 100, 0.8)"));

    mainScene.addEventListener("enterframe", function() {
        if (player.hp < 0 && enemy.hp < 0) {
            app.pushScene(ResultScene("draw"));
        } else if (player.hp < 0) {
            app.pushScene(ResultScene("lose"));
        } else if (enemy.hp < 0) {
            app.pushScene(ResultScene("win"));
        }
    });
};

var ResultScene = tm.createClass({
    superClass: tm.app.Scene,
    init: function(message) {
        this.superInit();
        var label = tm.app.Label(message);
        label.setAlign("center");
        label.setBaseline("middle");
        label.x = app.width/2;
        label.y = app.height/2;
        this.addChild(label);
    }
})

var Fighter = tm.createClass({
    superClass: tm.app.TriangleShape,
    force: null,
    hp: MAX_HP,
    init: function(x, y, color, index) {
        this.superInit(32, 32, {
            fillStyle: color,
            strokeStyle: "white",
            lineWidth: 2
        });
        this.x = x;
        this.y = y;
        this.alpha = 0.8;
        this.index = index;
        this.lastPointingEnd = -1;
    },
    update: function(app) {
        var enemy = fighters[rv(this.index)];
        var v = tm.geom.Vector2(enemy.x - this.x, enemy.y - this.y);
        this.rotation = -Math.atan2(v.x, v.y) * Math.RAD_TO_DEG + 180;

        if (app.frame % 4 === 0) {
            var s = tm.app.Sprite(128, 128, tm.graphics.TextureManager.get("texture"));
            s.setFrameIndex(12, 64, 64);
            s.x = this.x;
            s.y = this.y;
            s.blendMode = "lighter";
            s.update = function() {
                this.alpha -= 0.05;
                if (this.alpha < 0) {
                    this.remove();
                }
            };
            s.addChildTo(this.parent);
        }

        if (this.force) {
            this.position.add(this.force);
            this.force.mul(0.8);
            if (this.force.length() < 0.1) {
                this.force = null;
            }
        }

        if (tm.geom.Vector2(this.x - app.width/2, this.y - app.height/2).length() > RING_RADIUS) {
            this.hp -= DAMAGE_PAR_FRAME;
        } 

        if (this.x < 0) this.x = 0;
        else if (app.width < this.x) this.x = app.width;
        if (this.y < 0) this.y = 0;
        else if (app.height < this.y) this.y = app.height;
    },
    input: function(input) {
        if (input.pointing) {
            var vec = tm.geom.Vector2(input.pointingX - this.x, input.pointingY - this.y);
            if (vec.length() < FIGHTER_SPEED) {
                this.position.add(vec);
            } else {
                vec.normalize();
                this.position.add(vec.mul(FIGHTER_SPEED));
            }
        }

        if (input.pointingStart && Date.now() - this.lastPointingEnd < 200) {
            // fire
            var enemy = fighters[rv(this.index)];
            var v = tm.geom.Vector2(enemy.x - this.x, enemy.y - this.y).normalize().mul(BULLET_SPEED);
            this.parent.addChild(Bullet(this.x, this.y, v, enemy));
            tm.sound.WebAudioManager.get("shot").play();
        } else if (input.pointingEnd) {
            this.lastPointingEnd = Date.now();
        }
    },
})

var Bullet = tm.createClass({
    superClass: tm.app.Sprite,
    init: function(x, y, vec, target) {
        this.superInit(64, 64, tm.graphics.TextureManager.get("texture"));
        this.setFrameIndex(15, 64, 64);
        this.blendMode = "lighter";
        this.x = x;
        this.y = y;
        this.vec = vec;
        this.target = target;
    },
    update: function(app) {
        this.x += this.vec.x;
        this.y += this.vec.y;
        this.rotation = Math.atan2(this.vec.y, this.vec.x) * Math.RAD_TO_DEG + 90;

        // 衝突判定
        if (this.target.isHitPoint(this.x, this.y)) {
            // hit
            this.parent.addChild(ShockWave(this.x, this.y));
            this.target.force = this.vec.mul(BULLET_WEIGHT);
            this.remove();
            tm.sound.WebAudioManager.get("hit").play();
            return;
        }

        if (this.x < 0 || app.width < this.x || this.y < 0 || app.height < this.y) {
            this.remove();
            return;
        }
    },
});

var ShockWave = tm.createClass({
    superClass: tm.app.Sprite,
    init: function(x, y) {
        this.superInit(100, 100, tm.graphics.TextureManager.get("texture"));
        this.setFrameIndex(13, 64, 64);
        this.blendMode = "lighter";
        this.x = x;
        this.y = y;
        this.scale.x = 0.1;
        this.scale.y = 0.1;
    },
    update: function(app) {
        this.scale.x += 0.2;
        this.scale.y += 0.2;
        this.alpha -= 0.1;
        if (this.alpha < 0) {
            this.remove();
        }
    },
});

var Background = tm.createClass({
    superClass: tm.app.RectangleShape,
    rocks: [],
    init: function(app) {
        var gra = tm.graphics.LinearGradient(0, 0, 0, app.width);
        gra.addColorStopList([
            { offset: 0.0, color: "rgb( 0, 0, 0 )" },
            { offset: 1.0, color: "rgb( 0,80, 0 )" },
        ]);
        this.superInit(app.width, app.height, {
            fillStyle: gra.toStyle(),
            strokeStyle: "none"
        });

        var circle = tm.app.CircleShape(RING_RADIUS*2, RING_RADIUS*2, {
            fillStyle: "rgba(255, 255, 255, 0.1)",
            strokeStyle: "rgba(255, 255, 255, 0.5)",
        });
        circle.x = 0;
        circle.y = 0;
        this.addChild(circle);

        var rock = function() {
            var size = tm.util.Random.randint(1, 2);
            var n = tm.util.Random.randint(5, 8);
            var p = tm.app.PolygonShape(64, 64, {
                sides: n,
                fillStyle: "rgba(255,255,255,0.1)",
                strokeStyle: "rgba(255,255,255,0.3)",
                lineWidth: 1
            });
            p.scale.x = size;
            p.scale.y = size;
            p.x = tm.util.Random.randfloat(-app.width*0.8, app.width*0.8);
            p.y = tm.util.Random.randfloat(-app.height, app.height);
            p.speed = tm.util.Random.randint(5, 20);
            p.blendMode = "lighter";
            p.update = function() {
                this.rotation += 10;
                this.y += this.speed;
                if (app.height < this.y) {
                    this.y -= app.height*2;
                }
            };
            return p;
        };

        for (var i = 0; i < 20; i++) {
            this.addChild( rock() );
        }
    },
    update: function(app) {
        this.x = app.width/2 - this.parent.x;
        this.y = app.height/2 - this.parent.y;
    },
});

var HpBar = tm.createClass({
    superClass: tm.app.RectangleShape,
    init: function(fighter, y, color) {
        this.superInit(fighter.hp, 8, {
            fillStyle: color,
            strokeStyle: "none"
        });
        this.fighter = fighter;
        this.y = y;
    },
    update: function(app) {
        this.width = Math.max(1, this.fighter.hp);
        this.x = (MAX_HP - this.width) * -0.5 + app.width/2;
        if (this.fighter.hp < 0) {
            this.remove();
        }
    }
});

var rv = function(zeroOrOne) {
    return !zeroOrOne|0;
};

