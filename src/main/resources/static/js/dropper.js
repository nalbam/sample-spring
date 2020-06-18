/**
 * dropper.js
 */

class Dropper {
    constructor() {
        this.init();
    }

    init() {
        var canvas = document.getElementById("drop-canvas");
        if (canvas === null) {
            var width = window.innerWidth;
            var height = window.innerHeight;
            canvas = document.createElement("canvas");
            canvas.setAttribute("id", "drop-canvas");
            canvas.setAttribute("style", "display:block;z-index:999999;pointer-events:none;position:fixed;top:0");
            document.body.prepend(canvas);
            canvas.width = width;
            canvas.height = height;
            window.addEventListener("resize", function () {
                canvas.width = window.innerWidth;
                canvas.height = window.innerHeight;
            }, true);
        }

        this.debug = false;

        this.canvas = canvas;
        this.context = canvas.getContext("2d");

        this.particles = [];
        this.versions = [];

        // DodgerBlue, DarkOrange, ForestGreen, MediumPurple
        this.colors = ["30,144,255", "255,140,0", "34,139,34", "147,112,219"];
        this.error = "220,20,60"; // Crimson

        this.interval = 100;

        this.column = 50;
        this.radius = 10;
        this.alpha = 0.9;
        this.speed = 1;

        this.columns = [];
        for (var i = 0; i < this.column; i++) {
            this.columns.push(i);
        }
        this.columns.sort(function () {
            return 0.5 - Math.random()
        });
    }

    start() {
        if (!this.time) {
            this.time = performance.now();
        }

        if (!this.running) {
            this.running = true;
            requestAnimationFrame(this.step.bind(this));
        }
    }

    step(timestamp) {
        if (!this.running) {
            return;
        }

        var diff = timestamp - this.time;
        this.time = timestamp;

        this.draw(diff);

        requestAnimationFrame(this.step.bind(this));
    }

    draw(diff) {
        var width = window.innerWidth;
        var height = window.innerHeight;
        var column = parseInt(width / (this.radius * 3));
        var padding = parseInt(width / 6);

        this.context.clearRect(0, 0, width, height);

        var particle;
        var x;

        for (var i = 0; i < this.particles.length; i++) {
            particle = this.particles[i];

            particle.y += (diff * this.speed);
            if (particle.y > height) {
                this.del(particle.v, i);
                i--;
                continue;
            }

            x = parseInt(column / this.column * particle.x) * (this.radius * 2) + padding;

            this.context.beginPath();
            this.context.arc(x, particle.y, particle.r, 0, 2 * Math.PI);
            this.context.fillStyle = particle.color;
            this.context.fill();
        }
    }

    find(v) {
        var version;
        var index = -1;
        for (var i = 0; i < this.versions.length; i++) {
            version = this.versions[i];
            if (version.v === v) {
                index = i;
                break;
            }
        }
        // console.log(`find ${index}`);
        return index;
    }

    color(v) {
        var version;
        var color;

        var index = this.find(v);

        if (index > -1) {
            version = this.versions[index];
            version.x++;

            color = version.c;
        } else {
            if (v) {
                if (this.color_index) {
                    this.color_index++;
                    if (this.color_index >= this.colors.length) {
                        this.color_index = this.color_index % this.colors.length;
                    }
                } else {
                    this.color_index = parseInt(Math.random() * this.colors.length);
                }
                color = `rgba(${this.colors[this.color_index]},${this.alpha})`;
            } else {
                color = `rgba(${this.error},${this.alpha})`;
            }

            version = {};
            version.v = v;
            version.c = color;
            version.x = 1;

            this.versions.push(version);
        }

        if (this.debug) {
            console.log(`version ${this.versions.length} ${version.v} ${version.x}`);
        }

        return color;
    }

    del(v, i) {
        this.particles.splice(i, 1);

        var index = this.find(v);
        if (index > -1) {
            var version = this.versions[index];

            version.x--;

            if (version.x <= 0) {
                this.versions.splice(index, 1);
            }
        }
    }

    gen() {
        if (this.column_index) {
            this.column_index++;
            if (this.column_index >= this.columns.length) {
                this.column_index = this.column_index % this.columns.length;
            }
        } else {
            this.column_index = parseInt(Math.random() * this.columns.length);
        }

        return this.columns[this.column_index];
    }

    add(v) {
        var particle = {};

        particle.v = v;
        particle.x = this.gen();
        particle.y = this.radius * -1;
        particle.r = this.radius;
        particle.color = this.color(v);

        this.particles.push(particle);

        if (this.debug) {
            console.log(`drop ${this.particles.length} ${particle.x} ${particle.y} ${v}`);
        }
    }

    progress() {
        var e = document.getElementById("drop-rate");
        if (e) {
            var version;
            var width;
            var t = '<div class="progress">';
            for (var i = 0; i < this.versions.length; i++) {
                version = this.versions[i];
                width = version.x * 100 / this.particles.length;
                t += `<div class="progress-bar" role="progressbar" style="width:${width}%; background-color: ${version.c};"></div>`;
            }
            t += '</div>';
            e.innerHTML = t;
        }
    }
}

let dropper = new Dropper();

dropper.start();

function health() {
    var ms = Date.now();
    var url = `${location.protocol}//${location.host}/success/${rate}?q=${ms}`;
    $.ajax({
        url: url,
        type: 'get',
        success: function (res, status) {
            // console.log(`health : ${status}`);
            if (res) {
                dropper.add(res.version);
            } else {
                dropper.add(null);
            }
        },
        error: function (err) {
            dropper.add(null);
        }
    });
}

setInterval(function () {
    health();
}, dropper.interval);

setInterval(function () {
    dropper.progress();
}, 1000);
