// Thanks : particle.js
//----ANIMATED HEADER---//

! function(t, e) {
    "use strict";
    var i = t.GreenSockGlobals = t.GreenSockGlobals || t;
    if (!i.TweenLite) {
        var n, s, r, a, o, h = function(t) {
                var e, n = t.split("."),
                    s = i;
                for (e = 0; n.length > e; e++) s[n[e]] = s = s[n[e]] || {};
                return s
            },
            l = h("com.greensock"),
            _ = 1e-10,
            u = function(t) {
                var e, i = [],
                    n = t.length;
                for (e = 0; e !== n; i.push(t[e++]));
                return i
            },
            c = function() {},
            p = function() {
                var t = Object.prototype.toString,
                    e = t.call([]);
                return function(i) {
                    return null != i && (i instanceof Array || "object" == typeof i && !!i.push && t.call(i) === e)
                }
            }(),
            f = {},
            m = function(n, s, r, a) {
                this.sc = f[n] ? f[n].sc : [], f[n] = this, this.gsClass = null, this.func = r;
                var o = [];
                this.check = function(l) {
                    for (var _, u, c, p, d = s.length, g = d; --d > -1;)(_ = f[s[d]] || new m(s[d], [])).gsClass ? (o[d] = _.gsClass, g--) : l && _.sc.push(this);
                    if (0 === g && r)
                        for (u = ("com.greensock." + n).split("."), c = u.pop(), p = h(u.join("."))[c] = this.gsClass = r.apply(r, o), a && (i[c] = p, "function" == typeof define && define.amd ? define((t.GreenSockAMDPath ? t.GreenSockAMDPath + "/" : "") + n.split(".").pop(), [], function() {
                            return p
                        }) : n === e && "undefined" != typeof module && module.exports && (module.exports = p)), d = 0; this.sc.length > d; d++) this.sc[d].check()
                }, this.check(!0)
            },
            d = t._gsDefine = function(t, e, i, n) {
                return new m(t, e, i, n)
            },
            g = l._class = function(t, e, i) {
                return e = e || function() {}, d(t, [], function() {
                    return e
                }, i), e
            };
        d.globals = i;
        var v = [0, 0, 1, 1],
            w = [],
            y = g("easing.Ease", function(t, e, i, n) {
                this._func = t, this._type = i || 0, this._power = n || 0, this._params = e ? v.concat(e) : v
            }, !0),
            T = y.map = {},
            P = y.register = function(t, e, i, n) {
                for (var s, r, a, o, h = e.split(","), _ = h.length, u = (i || "easeIn,easeOut,easeInOut").split(","); --_ > -1;)
                    for (r = h[_], s = n ? g("easing." + r, null, !0) : l.easing[r] || {}, a = u.length; --a > -1;) o = u[a], T[r + "." + o] = T[o + r] = s[o] = t.getRatio ? t : t[o] || new t
            };
        for (r = y.prototype, r._calcEnd = !1, r.getRatio = function(t) {
            if (this._func) return this._params[0] = t, this._func.apply(null, this._params);
            var e = this._type,
                i = this._power,
                n = 1 === e ? 1 - t : 2 === e ? t : .5 > t ? 2 * t : 2 * (1 - t);
            return 1 === i ? n *= n : 2 === i ? n *= n * n : 3 === i ? n *= n * n * n : 4 === i && (n *= n * n * n * n), 1 === e ? 1 - n : 2 === e ? n : .5 > t ? n / 2 : 1 - n / 2
        }, n = ["Linear", "Quad", "Cubic", "Quart", "Quint,Strong"], s = n.length; --s > -1;) r = n[s] + ",Power" + s, P(new y(null, null, 1, s), r, "easeOut", !0), P(new y(null, null, 2, s), r, "easeIn" + (0 === s ? ",easeNone" : "")), P(new y(null, null, 3, s), r, "easeInOut");
        T.linear = l.easing.Linear.easeIn, T.swing = l.easing.Quad.easeInOut;
        var b = g("events.EventDispatcher", function(t) {
            this._listeners = {}, this._eventTarget = t || this
        });
        r = b.prototype, r.addEventListener = function(t, e, i, n, s) {
            s = s || 0;
            var r, h, l = this._listeners[t],
                _ = 0;
            for (null == l && (this._listeners[t] = l = []), h = l.length; --h > -1;) r = l[h], r.c === e && r.s === i ? l.splice(h, 1) : 0 === _ && s > r.pr && (_ = h + 1);
            l.splice(_, 0, {
                c: e,
                s: i,
                up: n,
                pr: s
            }), this !== a || o || a.wake()
        }, r.removeEventListener = function(t, e) {
            var i, n = this._listeners[t];
            if (n)
                for (i = n.length; --i > -1;)
                    if (n[i].c === e) return void n.splice(i, 1)
        }, r.dispatchEvent = function(t) {
            var e, i, n, s = this._listeners[t];
            if (s)
                for (e = s.length, i = this._eventTarget; --e > -1;) n = s[e], n.up ? n.c.call(n.s || i, {
                    type: t,
                    target: i
                }) : n.c.call(n.s || i)
        };
        var S = t.requestAnimationFrame,
            k = t.cancelAnimationFrame,
            x = Date.now || function() {
                return (new Date).getTime()
            },
            A = x();
        for (n = ["ms", "moz", "webkit", "o"], s = n.length; --s > -1 && !S;) S = t[n[s] + "RequestAnimationFrame"], k = t[n[s] + "CancelAnimationFrame"] || t[n[s] + "CancelRequestAnimationFrame"];
        g("Ticker", function(t, e) {
            var i, n, s, r, h, l = this,
                u = x(),
                p = e !== !1 && S,
                f = 500,
                m = 33,
                d = function(t) {
                    var e, a, o = x() - A;
                    o > f && (u += o - m), A += o, l.time = (A - u) / 1e3, e = l.time - h, (!i || e > 0 || t === !0) && (l.frame++, h += e + (e >= r ? .004 : r - e), a = !0), t !== !0 && (s = n(d)), a && l.dispatchEvent("tick")
                };
            b.call(l), l.time = l.frame = 0, l.tick = function() {
                d(!0)
            }, l.lagSmoothing = function(t, e) {
                f = t || 1 / _, m = Math.min(e, f, 0)
            }, l.sleep = function() {
                null != s && (p && k ? k(s) : clearTimeout(s), n = c, s = null, l === a && (o = !1))
            }, l.wake = function() {
                null !== s ? l.sleep() : l.frame > 10 && (A = x() - f + 5), n = 0 === i ? c : p && S ? S : function(t) {
                    return setTimeout(t, 0 | 1e3 * (h - l.time) + 1)
                }, l === a && (o = !0), d(2)
            }, l.fps = function(t) {
                return arguments.length ? (i = t, r = 1 / (i || 60), h = this.time + r, void l.wake()) : i
            }, l.useRAF = function(t) {
                return arguments.length ? (l.sleep(), p = t, void l.fps(i)) : p
            }, l.fps(t), setTimeout(function() {
                p && (!s || 5 > l.frame) && l.useRAF(!1)
            }, 1500)
        }), r = l.Ticker.prototype = new l.events.EventDispatcher, r.constructor = l.Ticker;
        var R = g("core.Animation", function(t, e) {
            if (this.vars = e = e || {}, this._duration = this._totalDuration = t || 0, this._delay = Number(e.delay) || 0, this._timeScale = 1, this._active = e.immediateRender === !0, this.data = e.data, this._reversed = e.reversed === !0, G) {
                o || a.wake();
                var i = this.vars.useFrames ? j : G;
                i.add(this, i._time), this.vars.paused && this.paused(!0)
            }
        });
        a = R.ticker = new l.Ticker, r = R.prototype, r._dirty = r._gc = r._initted = r._paused = !1, r._totalTime = r._time = 0, r._rawPrevTime = -1, r._next = r._last = r._onUpdate = r._timeline = r.timeline = null, r._paused = !1;
        var C = function() {
            o && x() - A > 2e3 && a.wake(), setTimeout(C, 2e3)
        };
        C(), r.play = function(t, e) {
            return null != t && this.seek(t, e), this.reversed(!1).paused(!1)
        }, r.pause = function(t, e) {
            return null != t && this.seek(t, e), this.paused(!0)
        }, r.resume = function(t, e) {
            return null != t && this.seek(t, e), this.paused(!1)
        }, r.seek = function(t, e) {
            return this.totalTime(Number(t), e !== !1)
        }, r.restart = function(t, e) {
            return this.reversed(!1).paused(!1).totalTime(t ? -this._delay : 0, e !== !1, !0)
        }, r.reverse = function(t, e) {
            return null != t && this.seek(t || this.totalDuration(), e), this.reversed(!0).paused(!1)
        }, r.render = function() {}, r.invalidate = function() {
            return this
        }, r.isActive = function() {
            var t, e = this._timeline,
                i = this._startTime;
            return !e || !this._gc && !this._paused && e.isActive() && (t = e.rawTime()) >= i && i + this.totalDuration() / this._timeScale > t
        }, r._enabled = function(t, e) {
            return o || a.wake(), this._gc = !t, this._active = this.isActive(), e !== !0 && (t && !this.timeline ? this._timeline.add(this, this._startTime - this._delay) : !t && this.timeline && this._timeline._remove(this, !0)), !1
        }, r._kill = function() {
            return this._enabled(!1, !1)
        }, r.kill = function(t, e) {
            return this._kill(t, e), this
        }, r._uncache = function(t) {
            for (var e = t ? this : this.timeline; e;) e._dirty = !0, e = e.timeline;
            return this
        }, r._swapSelfInParams = function(t) {
            for (var e = t.length, i = t.concat(); --e > -1;) "{self}" === t[e] && (i[e] = this);
            return i
        }, r.eventCallback = function(t, e, i, n) {
            if ("on" === (t || "").substr(0, 2)) {
                var s = this.vars;
                if (1 === arguments.length) return s[t];
                null == e ? delete s[t] : (s[t] = e, s[t + "Params"] = p(i) && -1 !== i.join("").indexOf("{self}") ? this._swapSelfInParams(i) : i, s[t + "Scope"] = n), "onUpdate" === t && (this._onUpdate = e)
            }
            return this
        }, r.delay = function(t) {
            return arguments.length ? (this._timeline.smoothChildTiming && this.startTime(this._startTime + t - this._delay), this._delay = t, this) : this._delay
        }, r.duration = function(t) {
            return arguments.length ? (this._duration = this._totalDuration = t, this._uncache(!0), this._timeline.smoothChildTiming && this._time > 0 && this._time < this._duration && 0 !== t && this.totalTime(this._totalTime * (t / this._duration), !0), this) : (this._dirty = !1, this._duration)
        }, r.totalDuration = function(t) {
            return this._dirty = !1, arguments.length ? this.duration(t) : this._totalDuration
        }, r.time = function(t, e) {
            return arguments.length ? (this._dirty && this.totalDuration(), this.totalTime(t > this._duration ? this._duration : t, e)) : this._time
        }, r.totalTime = function(t, e, i) {
            if (o || a.wake(), !arguments.length) return this._totalTime;
            if (this._timeline) {
                if (0 > t && !i && (t += this.totalDuration()), this._timeline.smoothChildTiming) {
                    this._dirty && this.totalDuration();
                    var n = this._totalDuration,
                        s = this._timeline;
                    if (t > n && !i && (t = n), this._startTime = (this._paused ? this._pauseTime : s._time) - (this._reversed ? n - t : t) / this._timeScale, s._dirty || this._uncache(!1), s._timeline)
                        for (; s._timeline;) s._timeline._time !== (s._startTime + s._totalTime) / s._timeScale && s.totalTime(s._totalTime, !0), s = s._timeline
                }
                this._gc && this._enabled(!0, !1), (this._totalTime !== t || 0 === this._duration) && (this.render(t, e, !1), D.length && Q())
            }
            return this
        }, r.progress = r.totalProgress = function(t, e) {
            return arguments.length ? this.totalTime(this.duration() * t, e) : this._time / this.duration()
        }, r.startTime = function(t) {
            return arguments.length ? (t !== this._startTime && (this._startTime = t, this.timeline && this.timeline._sortChildren && this.timeline.add(this, t - this._delay)), this) : this._startTime
        }, r.timeScale = function(t) {
            if (!arguments.length) return this._timeScale;
            if (t = t || _, this._timeline && this._timeline.smoothChildTiming) {
                var e = this._pauseTime,
                    i = e || 0 === e ? e : this._timeline.totalTime();
                this._startTime = i - (i - this._startTime) * this._timeScale / t
            }
            return this._timeScale = t, this._uncache(!1)
        }, r.reversed = function(t) {
            return arguments.length ? (t != this._reversed && (this._reversed = t, this.totalTime(this._timeline && !this._timeline.smoothChildTiming ? this.totalDuration() - this._totalTime : this._totalTime, !0)), this) : this._reversed
        }, r.paused = function(t) {
            if (!arguments.length) return this._paused;
            if (t != this._paused && this._timeline) {
                o || t || a.wake();
                var e = this._timeline,
                    i = e.rawTime(),
                    n = i - this._pauseTime;
                !t && e.smoothChildTiming && (this._startTime += n, this._uncache(!1)), this._pauseTime = t ? i : null, this._paused = t, this._active = this.isActive(), !t && 0 !== n && this._initted && this.duration() && this.render(e.smoothChildTiming ? this._totalTime : (i - this._startTime) / this._timeScale, !0, !0)
            }
            return this._gc && !t && this._enabled(!0, !1), this
        };
        var E = g("core.SimpleTimeline", function(t) {
            R.call(this, 0, t), this.autoRemoveChildren = this.smoothChildTiming = !0
        });
        r = E.prototype = new R, r.constructor = E, r.kill()._gc = !1, r._first = r._last = null, r._sortChildren = !1, r.add = r.insert = function(t, e) {
            var i, n;
            if (t._startTime = Number(e || 0) + t._delay, t._paused && this !== t._timeline && (t._pauseTime = t._startTime + (this.rawTime() - t._startTime) / t._timeScale), t.timeline && t.timeline._remove(t, !0), t.timeline = t._timeline = this, t._gc && t._enabled(!0, !0), i = this._last, this._sortChildren)
                for (n = t._startTime; i && i._startTime > n;) i = i._prev;
            return i ? (t._next = i._next, i._next = t) : (t._next = this._first, this._first = t), t._next ? t._next._prev = t : this._last = t, t._prev = i, this._timeline && this._uncache(!0), this
        }, r._remove = function(t, e) {
            return t.timeline === this && (e || t._enabled(!1, !0), t._prev ? t._prev._next = t._next : this._first === t && (this._first = t._next), t._next ? t._next._prev = t._prev : this._last === t && (this._last = t._prev), t._next = t._prev = t.timeline = null, this._timeline && this._uncache(!0)), this
        }, r.render = function(t, e, i) {
            var n, s = this._first;
            for (this._totalTime = this._time = this._rawPrevTime = t; s;) n = s._next, (s._active || t >= s._startTime && !s._paused) && (s._reversed ? s.render((s._dirty ? s.totalDuration() : s._totalDuration) - (t - s._startTime) * s._timeScale, e, i) : s.render((t - s._startTime) * s._timeScale, e, i)), s = n
        }, r.rawTime = function() {
            return o || a.wake(), this._totalTime
        };
        var I = g("TweenLite", function(e, i, n) {
                if (R.call(this, i, n), this.render = I.prototype.render, null == e) throw "Cannot tween a null target.";
                this.target = e = "string" != typeof e ? e : I.selector(e) || e;
                var s, r, a, o = e.jquery || e.length && e !== t && e[0] && (e[0] === t || e[0].nodeType && e[0].style && !e.nodeType),
                    h = this.vars.overwrite;
                if (this._overwrite = h = null == h ? U[I.defaultOverwrite] : "number" == typeof h ? h >> 0 : U[h], (o || e instanceof Array || e.push && p(e)) && "number" != typeof e[0])
                    for (this._targets = a = u(e), this._propLookup = [], this._siblings = [], s = 0; a.length > s; s++) r = a[s], r ? "string" != typeof r ? r.length && r !== t && r[0] && (r[0] === t || r[0].nodeType && r[0].style && !r.nodeType) ? (a.splice(s--, 1), this._targets = a = a.concat(u(r))) : (this._siblings[s] = X(r, this, !1), 1 === h && this._siblings[s].length > 1 && Y(r, this, null, 1, this._siblings[s])) : (r = a[s--] = I.selector(r), "string" == typeof r && a.splice(s + 1, 1)) : a.splice(s--, 1);
                else this._propLookup = {}, this._siblings = X(e, this, !1), 1 === h && this._siblings.length > 1 && Y(e, this, null, 1, this._siblings);
                (this.vars.immediateRender || 0 === i && 0 === this._delay && this.vars.immediateRender !== !1) && (this._time = -_, this.render(-this._delay))
            }, !0),
            M = function(e) {
                return e.length && e !== t && e[0] && (e[0] === t || e[0].nodeType && e[0].style && !e.nodeType)
            },
            O = function(t, e) {
                var i, n = {};
                for (i in t) N[i] || i in e && "transform" !== i && "x" !== i && "y" !== i && "width" !== i && "height" !== i && "className" !== i && "border" !== i || !(!L[i] || L[i] && L[i]._autoCSS) || (n[i] = t[i], delete t[i]);
                t.css = n
            };
        r = I.prototype = new R, r.constructor = I, r.kill()._gc = !1, r.ratio = 0, r._firstPT = r._targets = r._overwrittenProps = r._startAt = null, r._notifyPluginsOfEnabled = r._lazy = !1, I.version = "1.13.1", I.defaultEase = r._ease = new y(null, null, 1, 1), I.defaultOverwrite = "auto", I.ticker = a, I.autoSleep = !0, I.lagSmoothing = function(t, e) {
            a.lagSmoothing(t, e)
        }, I.selector = t.$ || t.jQuery || function(e) {
            var i = t.$ || t.jQuery;
            return i ? (I.selector = i, i(e)) : "undefined" == typeof document ? e : document.querySelectorAll ? document.querySelectorAll(e) : document.getElementById("#" === e.charAt(0) ? e.substr(1) : e)
        };
        var D = [],
            z = {},
            F = I._internals = {
                isArray: p,
                isSelector: M,
                lazyTweens: D
            },
            L = I._plugins = {},
            q = F.tweenLookup = {},
            B = 0,
            N = F.reservedProps = {
                ease: 1,
                delay: 1,
                overwrite: 1,
                onComplete: 1,
                onCompleteParams: 1,
                onCompleteScope: 1,
                useFrames: 1,
                runBackwards: 1,
                startAt: 1,
                onUpdate: 1,
                onUpdateParams: 1,
                onUpdateScope: 1,
                onStart: 1,
                onStartParams: 1,
                onStartScope: 1,
                onReverseComplete: 1,
                onReverseCompleteParams: 1,
                onReverseCompleteScope: 1,
                onRepeat: 1,
                onRepeatParams: 1,
                onRepeatScope: 1,
                easeParams: 1,
                yoyo: 1,
                immediateRender: 1,
                repeat: 1,
                repeatDelay: 1,
                data: 1,
                paused: 1,
                reversed: 1,
                autoCSS: 1,
                lazy: 1
            },
            U = {
                none: 0,
                all: 1,
                auto: 2,
                concurrent: 3,
                allOnStart: 4,
                preexisting: 5,
                "true": 1,
                "false": 0
            },
            j = R._rootFramesTimeline = new E,
            G = R._rootTimeline = new E,
            Q = F.lazyRender = function() {
                var t = D.length;
                for (z = {}; --t > -1;) n = D[t], n && n._lazy !== !1 && (n.render(n._lazy, !1, !0), n._lazy = !1);
                D.length = 0
            };
        G._startTime = a.time, j._startTime = a.frame, G._active = j._active = !0, setTimeout(Q, 1), R._updateRoot = I.render = function() {
            var t, e, i;
            if (D.length && Q(), G.render((a.time - G._startTime) * G._timeScale, !1, !1), j.render((a.frame - j._startTime) * j._timeScale, !1, !1), D.length && Q(), !(a.frame % 120)) {
                for (i in q) {
                    for (e = q[i].tweens, t = e.length; --t > -1;) e[t]._gc && e.splice(t, 1);
                    0 === e.length && delete q[i]
                }
                if (i = G._first, (!i || i._paused) && I.autoSleep && !j._first && 1 === a._listeners.tick.length) {
                    for (; i && i._paused;) i = i._next;
                    i || a.sleep()
                }
            }
        }, a.addEventListener("tick", R._updateRoot);
        var X = function(t, e, i) {
                var n, s, r = t._gsTweenID;
                if (q[r || (t._gsTweenID = r = "t" + B++)] || (q[r] = {
                    target: t,
                    tweens: []
                }), e && (n = q[r].tweens, n[s = n.length] = e, i))
                    for (; --s > -1;) n[s] === e && n.splice(s, 1);
                return q[r].tweens
            },
            Y = function(t, e, i, n, s) {
                var r, a, o, h;
                if (1 === n || n >= 4) {
                    for (h = s.length, r = 0; h > r; r++)
                        if ((o = s[r]) !== e) o._gc || o._enabled(!1, !1) && (a = !0);
                        else if (5 === n) break;
                    return a
                }
                var l, u = e._startTime + _,
                    c = [],
                    p = 0,
                    f = 0 === e._duration;
                for (r = s.length; --r > -1;)(o = s[r]) === e || o._gc || o._paused || (o._timeline !== e._timeline ? (l = l || H(e, 0, f), 0 === H(o, l, f) && (c[p++] = o)) : u >= o._startTime && o._startTime + o.totalDuration() / o._timeScale > u && ((f || !o._initted) && 2e-10 >= u - o._startTime || (c[p++] = o)));
                for (r = p; --r > -1;) o = c[r], 2 === n && o._kill(i, t) && (a = !0), (2 !== n || !o._firstPT && o._initted) && o._enabled(!1, !1) && (a = !0);
                return a
            },
            H = function(t, e, i) {
                for (var n = t._timeline, s = n._timeScale, r = t._startTime; n._timeline;) {
                    if (r += n._startTime, s *= n._timeScale, n._paused) return -100;
                    n = n._timeline
                }
                return r /= s, r > e ? r - e : i && r === e || !t._initted && 2 * _ > r - e ? _ : (r += t.totalDuration() / t._timeScale / s) > e + _ ? 0 : r - e - _
            };
        r._init = function() {
            var t, e, i, n, s, r = this.vars,
                a = this._overwrittenProps,
                o = this._duration,
                h = !!r.immediateRender,
                l = r.ease;
            if (r.startAt) {
                this._startAt && (this._startAt.render(-1, !0), this._startAt.kill()), s = {};
                for (n in r.startAt) s[n] = r.startAt[n];
                if (s.overwrite = !1, s.immediateRender = !0, s.lazy = h && r.lazy !== !1, s.startAt = s.delay = null, this._startAt = I.to(this.target, 0, s), h)
                    if (this._time > 0) this._startAt = null;
                    else if (0 !== o) return
            } else if (r.runBackwards && 0 !== o)
                if (this._startAt) this._startAt.render(-1, !0), this._startAt.kill(), this._startAt = null;
                else {
                    i = {};
                    for (n in r) N[n] && "autoCSS" !== n || (i[n] = r[n]);
                    if (i.overwrite = 0, i.data = "isFromStart", i.lazy = h && r.lazy !== !1, i.immediateRender = h, this._startAt = I.to(this.target, 0, i), h) {
                        if (0 === this._time) return
                    } else this._startAt._init(), this._startAt._enabled(!1)
                }
            if (this._ease = l = l ? l instanceof y ? l : "function" == typeof l ? new y(l, r.easeParams) : T[l] || I.defaultEase : I.defaultEase, r.easeParams instanceof Array && l.config && (this._ease = l.config.apply(l, r.easeParams)), this._easeType = this._ease._type, this._easePower = this._ease._power, this._firstPT = null, this._targets)
                for (t = this._targets.length; --t > -1;) this._initProps(this._targets[t], this._propLookup[t] = {}, this._siblings[t], a ? a[t] : null) && (e = !0);
            else e = this._initProps(this.target, this._propLookup, this._siblings, a);
            if (e && I._onPluginEvent("_onInitAllProps", this), a && (this._firstPT || "function" != typeof this.target && this._enabled(!1, !1)), r.runBackwards)
                for (i = this._firstPT; i;) i.s += i.c, i.c = -i.c, i = i._next;
            this._onUpdate = r.onUpdate, this._initted = !0
        }, r._initProps = function(e, i, n, s) {
            var r, a, o, h, l, _;
            if (null == e) return !1;
            z[e._gsTweenID] && Q(), this.vars.css || e.style && e !== t && e.nodeType && L.css && this.vars.autoCSS !== !1 && O(this.vars, e);
            for (r in this.vars) {
                if (_ = this.vars[r], N[r]) _ && (_ instanceof Array || _.push && p(_)) && -1 !== _.join("").indexOf("{self}") && (this.vars[r] = _ = this._swapSelfInParams(_, this));
                else if (L[r] && (h = new L[r])._onInitTween(e, this.vars[r], this)) {
                    for (this._firstPT = l = {
                        _next: this._firstPT,
                        t: h,
                        p: "setRatio",
                        s: 0,
                        c: 1,
                        f: !0,
                        n: r,
                        pg: !0,
                        pr: h._priority
                    }, a = h._overwriteProps.length; --a > -1;) i[h._overwriteProps[a]] = this._firstPT;
                    (h._priority || h._onInitAllProps) && (o = !0), (h._onDisable || h._onEnable) && (this._notifyPluginsOfEnabled = !0)
                } else this._firstPT = i[r] = l = {
                    _next: this._firstPT,
                    t: e,
                    p: r,
                    f: "function" == typeof e[r],
                    n: r,
                    pg: !1,
                    pr: 0
                }, l.s = l.f ? e[r.indexOf("set") || "function" != typeof e["get" + r.substr(3)] ? r : "get" + r.substr(3)]() : parseFloat(e[r]), l.c = "string" == typeof _ && "=" === _.charAt(1) ? parseInt(_.charAt(0) + "1", 10) * Number(_.substr(2)) : Number(_) - l.s || 0;
                l && l._next && (l._next._prev = l)
            }
            return s && this._kill(s, e) ? this._initProps(e, i, n, s) : this._overwrite > 1 && this._firstPT && n.length > 1 && Y(e, this, i, this._overwrite, n) ? (this._kill(i, e), this._initProps(e, i, n, s)) : (this._firstPT && (this.vars.lazy !== !1 && this._duration || this.vars.lazy && !this._duration) && (z[e._gsTweenID] = !0), o)
        }, r.render = function(t, e, i) {
            var n, s, r, a, o = this._time,
                h = this._duration,
                l = this._rawPrevTime;
            if (t >= h) this._totalTime = this._time = h, this.ratio = this._ease._calcEnd ? this._ease.getRatio(1) : 1, this._reversed || (n = !0, s = "onComplete"), 0 === h && (this._initted || !this.vars.lazy || i) && (this._startTime === this._timeline._duration && (t = 0), (0 === t || 0 > l || l === _) && l !== t && (i = !0, l > _ && (s = "onReverseComplete")), this._rawPrevTime = a = !e || t || l === t ? t : _);
            else if (1e-7 > t) this._totalTime = this._time = 0, this.ratio = this._ease._calcEnd ? this._ease.getRatio(0) : 0, (0 !== o || 0 === h && l > 0 && l !== _) && (s = "onReverseComplete", n = this._reversed), 0 > t ? (this._active = !1, 0 === h && (this._initted || !this.vars.lazy || i) && (l >= 0 && (i = !0), this._rawPrevTime = a = !e || t || l === t ? t : _)) : this._initted || (i = !0);
            else if (this._totalTime = this._time = t, this._easeType) {
                var u = t / h,
                    c = this._easeType,
                    p = this._easePower;
                (1 === c || 3 === c && u >= .5) && (u = 1 - u), 3 === c && (u *= 2), 1 === p ? u *= u : 2 === p ? u *= u * u : 3 === p ? u *= u * u * u : 4 === p && (u *= u * u * u * u), this.ratio = 1 === c ? 1 - u : 2 === c ? u : .5 > t / h ? u / 2 : 1 - u / 2
            } else this.ratio = this._ease.getRatio(t / h);
            if (this._time !== o || i) {
                if (!this._initted) {
                    if (this._init(), !this._initted || this._gc) return;
                    if (!i && this._firstPT && (this.vars.lazy !== !1 && this._duration || this.vars.lazy && !this._duration)) return this._time = this._totalTime = o, this._rawPrevTime = l, D.push(this), void(this._lazy = t);
                    this._time && !n ? this.ratio = this._ease.getRatio(this._time / h) : n && this._ease._calcEnd && (this.ratio = this._ease.getRatio(0 === this._time ? 0 : 1))
                }
                for (this._lazy !== !1 && (this._lazy = !1), this._active || !this._paused && this._time !== o && t >= 0 && (this._active = !0), 0 === o && (this._startAt && (t >= 0 ? this._startAt.render(t, e, i) : s || (s = "_dummyGS")), this.vars.onStart && (0 !== this._time || 0 === h) && (e || this.vars.onStart.apply(this.vars.onStartScope || this, this.vars.onStartParams || w))), r = this._firstPT; r;) r.f ? r.t[r.p](r.c * this.ratio + r.s) : r.t[r.p] = r.c * this.ratio + r.s, r = r._next;
                this._onUpdate && (0 > t && this._startAt && this._startTime && this._startAt.render(t, e, i), e || (this._time !== o || n) && this._onUpdate.apply(this.vars.onUpdateScope || this, this.vars.onUpdateParams || w)), s && (!this._gc || i) && (0 > t && this._startAt && !this._onUpdate && this._startTime && this._startAt.render(t, e, i), n && (this._timeline.autoRemoveChildren && this._enabled(!1, !1), this._active = !1), !e && this.vars[s] && this.vars[s].apply(this.vars[s + "Scope"] || this, this.vars[s + "Params"] || w), 0 === h && this._rawPrevTime === _ && a !== _ && (this._rawPrevTime = 0))
            }
        }, r._kill = function(t, e) {
            if ("all" === t && (t = null), null == t && (null == e || e === this.target)) return this._lazy = !1, this._enabled(!1, !1);
            e = "string" != typeof e ? e || this._targets || this.target : I.selector(e) || e;
            var i, n, s, r, a, o, h, l;
            if ((p(e) || M(e)) && "number" != typeof e[0])
                for (i = e.length; --i > -1;) this._kill(t, e[i]) && (o = !0);
            else {
                if (this._targets) {
                    for (i = this._targets.length; --i > -1;)
                        if (e === this._targets[i]) {
                            a = this._propLookup[i] || {}, this._overwrittenProps = this._overwrittenProps || [], n = this._overwrittenProps[i] = t ? this._overwrittenProps[i] || {} : "all";
                            break
                        }
                } else {
                    if (e !== this.target) return !1;
                    a = this._propLookup, n = this._overwrittenProps = t ? this._overwrittenProps || {} : "all"
                }
                if (a) {
                    h = t || a, l = t !== n && "all" !== n && t !== a && ("object" != typeof t || !t._tempKill);
                    for (s in h)(r = a[s]) && (r.pg && r.t._kill(h) && (o = !0), r.pg && 0 !== r.t._overwriteProps.length || (r._prev ? r._prev._next = r._next : r === this._firstPT && (this._firstPT = r._next), r._next && (r._next._prev = r._prev), r._next = r._prev = null), delete a[s]), l && (n[s] = 1);
                    !this._firstPT && this._initted && this._enabled(!1, !1)
                }
            }
            return o
        }, r.invalidate = function() {
            return this._notifyPluginsOfEnabled && I._onPluginEvent("_onDisable", this), this._firstPT = null, this._overwrittenProps = null, this._onUpdate = null, this._startAt = null, this._initted = this._active = this._notifyPluginsOfEnabled = this._lazy = !1, this._propLookup = this._targets ? {} : [], this
        }, r._enabled = function(t, e) {
            if (o || a.wake(), t && this._gc) {
                var i, n = this._targets;
                if (n)
                    for (i = n.length; --i > -1;) this._siblings[i] = X(n[i], this, !0);
                else this._siblings = X(this.target, this, !0)
            }
            return R.prototype._enabled.call(this, t, e), this._notifyPluginsOfEnabled && this._firstPT ? I._onPluginEvent(t ? "_onEnable" : "_onDisable", this) : !1
        }, I.to = function(t, e, i) {
            return new I(t, e, i)
        }, I.from = function(t, e, i) {
            return i.runBackwards = !0, i.immediateRender = 0 != i.immediateRender, new I(t, e, i)
        }, I.fromTo = function(t, e, i, n) {
            return n.startAt = i, n.immediateRender = 0 != n.immediateRender && 0 != i.immediateRender, new I(t, e, n)
        }, I.delayedCall = function(t, e, i, n, s) {
            return new I(e, 0, {
                delay: t,
                onComplete: e,
                onCompleteParams: i,
                onCompleteScope: n,
                onReverseComplete: e,
                onReverseCompleteParams: i,
                onReverseCompleteScope: n,
                immediateRender: !1,
                useFrames: s,
                overwrite: 0
            })
        }, I.set = function(t, e) {
            return new I(t, 0, e)
        }, I.getTweensOf = function(t, e) {
            if (null == t) return [];
            t = "string" != typeof t ? t : I.selector(t) || t;
            var i, n, s, r;
            if ((p(t) || M(t)) && "number" != typeof t[0]) {
                for (i = t.length, n = []; --i > -1;) n = n.concat(I.getTweensOf(t[i], e));
                for (i = n.length; --i > -1;)
                    for (r = n[i], s = i; --s > -1;) r === n[s] && n.splice(i, 1)
            } else
                for (n = X(t).concat(), i = n.length; --i > -1;)(n[i]._gc || e && !n[i].isActive()) && n.splice(i, 1);
            return n
        }, I.killTweensOf = I.killDelayedCallsTo = function(t, e, i) {
            "object" == typeof e && (i = e, e = !1);
            for (var n = I.getTweensOf(t, e), s = n.length; --s > -1;) n[s]._kill(i, t)
        };
        var W = g("plugins.TweenPlugin", function(t, e) {
            this._overwriteProps = (t || "").split(","), this._propName = this._overwriteProps[0], this._priority = e || 0, this._super = W.prototype
        }, !0);
        if (r = W.prototype, W.version = "1.10.1", W.API = 2, r._firstPT = null, r._addTween = function(t, e, i, n, s, r) {
            var a, o;
            return null != n && (a = "number" == typeof n || "=" !== n.charAt(1) ? Number(n) - i : parseInt(n.charAt(0) + "1", 10) * Number(n.substr(2))) ? (this._firstPT = o = {
                _next: this._firstPT,
                t: t,
                p: e,
                s: i,
                c: a,
                f: "function" == typeof t[e],
                n: s || e,
                r: r
            }, o._next && (o._next._prev = o), o) : void 0
        }, r.setRatio = function(t) {
            for (var e, i = this._firstPT, n = 1e-6; i;) e = i.c * t + i.s, i.r ? e = Math.round(e) : n > e && e > -n && (e = 0), i.f ? i.t[i.p](e) : i.t[i.p] = e, i = i._next
        }, r._kill = function(t) {
            var e, i = this._overwriteProps,
                n = this._firstPT;
            if (null != t[this._propName]) this._overwriteProps = [];
            else
                for (e = i.length; --e > -1;) null != t[i[e]] && i.splice(e, 1);
            for (; n;) null != t[n.n] && (n._next && (n._next._prev = n._prev), n._prev ? (n._prev._next = n._next, n._prev = null) : this._firstPT === n && (this._firstPT = n._next)), n = n._next;
            return !1
        }, r._roundProps = function(t, e) {
            for (var i = this._firstPT; i;)(t[this._propName] || null != i.n && t[i.n.split(this._propName + "_").join("")]) && (i.r = e), i = i._next
        }, I._onPluginEvent = function(t, e) {
            var i, n, s, r, a, o = e._firstPT;
            if ("_onInitAllProps" === t) {
                for (; o;) {
                    for (a = o._next, n = s; n && n.pr > o.pr;) n = n._next;
                    (o._prev = n ? n._prev : r) ? o._prev._next = o: s = o, (o._next = n) ? n._prev = o : r = o, o = a
                }
                o = e._firstPT = s
            }
            for (; o;) o.pg && "function" == typeof o.t[t] && o.t[t]() && (i = !0), o = o._next;
            return i
        }, W.activate = function(t) {
            for (var e = t.length; --e > -1;) t[e].API === W.API && (L[(new t[e])._propName] = t[e]);
            return !0
        }, d.plugin = function(t) {
            if (!(t && t.propName && t.init && t.API)) throw "illegal plugin definition.";
            var e, i = t.propName,
                n = t.priority || 0,
                s = t.overwriteProps,
                r = {
                    init: "_onInitTween",
                    set: "setRatio",
                    kill: "_kill",
                    round: "_roundProps",
                    initAll: "_onInitAllProps"
                },
                a = g("plugins." + i.charAt(0).toUpperCase() + i.substr(1) + "Plugin", function() {
                    W.call(this, i, n), this._overwriteProps = s || []
                }, t.global === !0),
                o = a.prototype = new W(i);
            o.constructor = a, a.API = t.API;
            for (e in r) "function" == typeof t[e] && (o[r[e]] = t[e]);
            return a.version = t.version, W.activate([a]), a
        }, n = t._gsQueue) {
            for (s = 0; n.length > s; s++) n[s]();
            for (r in f) f[r].func || t.console.log("GSAP encountered missing dependency: com.greensock." + r)
        }
        o = !1
    }
}("undefined" != typeof module && module.exports && "undefined" != typeof global ? global : this || window, "TweenLite");
var _gsScope = "undefined" != typeof module && module.exports && "undefined" != typeof global ? global : this || window;
(_gsScope._gsQueue || (_gsScope._gsQueue = [])).push(function() {
    "use strict";
    _gsScope._gsDefine("easing.Back", ["easing.Ease"], function(t) {
        var e, i, n, s = _gsScope.GreenSockGlobals || _gsScope,
            r = s.com.greensock,
            a = 2 * Math.PI,
            o = Math.PI / 2,
            h = r._class,
            l = function(e, i) {
                var n = h("easing." + e, function() {}, !0),
                    s = n.prototype = new t;
                return s.constructor = n, s.getRatio = i, n
            },
            _ = t.register || function() {},
            u = function(t, e, i, n) {
                var s = h("easing." + t, {
                    easeOut: new e,
                    easeIn: new i,
                    easeInOut: new n
                }, !0);
                return _(s, t), s
            },
            c = function(t, e, i) {
                this.t = t, this.v = e, i && (this.next = i, i.prev = this, this.c = i.v - e, this.gap = i.t - t)
            },
            p = function(e, i) {
                var n = h("easing." + e, function(t) {
                        this._p1 = t || 0 === t ? t : 1.70158, this._p2 = 1.525 * this._p1
                    }, !0),
                    s = n.prototype = new t;
                return s.constructor = n, s.getRatio = i, s.config = function(t) {
                    return new n(t)
                }, n
            },
            f = u("Back", p("BackOut", function(t) {
                return (t -= 1) * t * ((this._p1 + 1) * t + this._p1) + 1
            }), p("BackIn", function(t) {
                return t * t * ((this._p1 + 1) * t - this._p1)
            }), p("BackInOut", function(t) {
                return 1 > (t *= 2) ? .5 * t * t * ((this._p2 + 1) * t - this._p2) : .5 * ((t -= 2) * t * ((this._p2 + 1) * t + this._p2) + 2)
            })),
            m = h("easing.SlowMo", function(t, e, i) {
                e = e || 0 === e ? e : .7, null == t ? t = .7 : t > 1 && (t = 1), this._p = 1 !== t ? e : 0, this._p1 = (1 - t) / 2, this._p2 = t, this._p3 = this._p1 + this._p2, this._calcEnd = i === !0
            }, !0),
            d = m.prototype = new t;
        return d.constructor = m, d.getRatio = function(t) {
            var e = t + (.5 - t) * this._p;
            return this._p1 > t ? this._calcEnd ? 1 - (t = 1 - t / this._p1) * t : e - (t = 1 - t / this._p1) * t * t * t * e : t > this._p3 ? this._calcEnd ? 1 - (t = (t - this._p3) / this._p1) * t : e + (t - e) * (t = (t - this._p3) / this._p1) * t * t * t : this._calcEnd ? 1 : e
        }, m.ease = new m(.7, .7), d.config = m.config = function(t, e, i) {
            return new m(t, e, i)
        }, e = h("easing.SteppedEase", function(t) {
            t = t || 1, this._p1 = 1 / t, this._p2 = t + 1
        }, !0), d = e.prototype = new t, d.constructor = e, d.getRatio = function(t) {
            return 0 > t ? t = 0 : t >= 1 && (t = .999999999), (this._p2 * t >> 0) * this._p1
        }, d.config = e.config = function(t) {
            return new e(t)
        }, i = h("easing.RoughEase", function(e) {
            e = e || {};
            for (var i, n, s, r, a, o, h = e.taper || "none", l = [], _ = 0, u = 0 | (e.points || 20), p = u, f = e.randomize !== !1, m = e.clamp === !0, d = e.template instanceof t ? e.template : null, g = "number" == typeof e.strength ? .4 * e.strength : .4; --p > -1;) i = f ? Math.random() : 1 / u * p, n = d ? d.getRatio(i) : i, "none" === h ? s = g : "out" === h ? (r = 1 - i, s = r * r * g) : "in" === h ? s = i * i * g : .5 > i ? (r = 2 * i, s = .5 * r * r * g) : (r = 2 * (1 - i), s = .5 * r * r * g), f ? n += Math.random() * s - .5 * s : p % 2 ? n += .5 * s : n -= .5 * s, m && (n > 1 ? n = 1 : 0 > n && (n = 0)), l[_++] = {
                x: i,
                y: n
            };
            for (l.sort(function(t, e) {
                return t.x - e.x
            }), o = new c(1, 1, null), p = u; --p > -1;) a = l[p], o = new c(a.x, a.y, o);
            this._prev = new c(0, 0, 0 !== o.t ? o : o.next)
        }, !0), d = i.prototype = new t, d.constructor = i, d.getRatio = function(t) {
            var e = this._prev;
            if (t > e.t) {
                for (; e.next && t >= e.t;) e = e.next;
                e = e.prev
            } else
                for (; e.prev && e.t >= t;) e = e.prev;
            return this._prev = e, e.v + (t - e.t) / e.gap * e.c
        }, d.config = function(t) {
            return new i(t)
        }, i.ease = new i, u("Bounce", l("BounceOut", function(t) {
            return 1 / 2.75 > t ? 7.5625 * t * t : 2 / 2.75 > t ? 7.5625 * (t -= 1.5 / 2.75) * t + .75 : 2.5 / 2.75 > t ? 7.5625 * (t -= 2.25 / 2.75) * t + .9375 : 7.5625 * (t -= 2.625 / 2.75) * t + .984375
        }), l("BounceIn", function(t) {
            return 1 / 2.75 > (t = 1 - t) ? 1 - 7.5625 * t * t : 2 / 2.75 > t ? 1 - (7.5625 * (t -= 1.5 / 2.75) * t + .75) : 2.5 / 2.75 > t ? 1 - (7.5625 * (t -= 2.25 / 2.75) * t + .9375) : 1 - (7.5625 * (t -= 2.625 / 2.75) * t + .984375)
        }), l("BounceInOut", function(t) {
            var e = .5 > t;
            return t = e ? 1 - 2 * t : 2 * t - 1, t = 1 / 2.75 > t ? 7.5625 * t * t : 2 / 2.75 > t ? 7.5625 * (t -= 1.5 / 2.75) * t + .75 : 2.5 / 2.75 > t ? 7.5625 * (t -= 2.25 / 2.75) * t + .9375 : 7.5625 * (t -= 2.625 / 2.75) * t + .984375, e ? .5 * (1 - t) : .5 * t + .5
        })), u("Circ", l("CircOut", function(t) {
            return Math.sqrt(1 - (t -= 1) * t)
        }), l("CircIn", function(t) {
            return -(Math.sqrt(1 - t * t) - 1)
        }), l("CircInOut", function(t) {
            return 1 > (t *= 2) ? -.5 * (Math.sqrt(1 - t * t) - 1) : .5 * (Math.sqrt(1 - (t -= 2) * t) + 1)
        })), n = function(e, i, n) {
            var s = h("easing." + e, function(t, e) {
                    this._p1 = t || 1, this._p2 = e || n, this._p3 = this._p2 / a * (Math.asin(1 / this._p1) || 0)
                }, !0),
                r = s.prototype = new t;
            return r.constructor = s, r.getRatio = i, r.config = function(t, e) {
                return new s(t, e)
            }, s
        }, u("Elastic", n("ElasticOut", function(t) {
            return this._p1 * Math.pow(2, -10 * t) * Math.sin((t - this._p3) * a / this._p2) + 1
        }, .3), n("ElasticIn", function(t) {
            return -(this._p1 * Math.pow(2, 10 * (t -= 1)) * Math.sin((t - this._p3) * a / this._p2))
        }, .3), n("ElasticInOut", function(t) {
            return 1 > (t *= 2) ? -.5 * this._p1 * Math.pow(2, 10 * (t -= 1)) * Math.sin((t - this._p3) * a / this._p2) : .5 * this._p1 * Math.pow(2, -10 * (t -= 1)) * Math.sin((t - this._p3) * a / this._p2) + 1
        }, .45)), u("Expo", l("ExpoOut", function(t) {
            return 1 - Math.pow(2, -10 * t)
        }), l("ExpoIn", function(t) {
            return Math.pow(2, 10 * (t - 1)) - .001
        }), l("ExpoInOut", function(t) {
            return 1 > (t *= 2) ? .5 * Math.pow(2, 10 * (t - 1)) : .5 * (2 - Math.pow(2, -10 * (t - 1)))
        })), u("Sine", l("SineOut", function(t) {
            return Math.sin(t * o)
        }), l("SineIn", function(t) {
            return -Math.cos(t * o) + 1
        }), l("SineInOut", function(t) {
            return -.5 * (Math.cos(Math.PI * t) - 1)
        })), h("easing.EaseLookup", {
            find: function(e) {
                return t.map[e]
            }
        }, !0), _(s.SlowMo, "SlowMo", "ease,"), _(i, "RoughEase", "ease,"), _(e, "SteppedEase", "ease,"), f
    }, !0)
}), _gsScope._gsDefine && _gsScope._gsQueue.pop()(),
    function() {
        for (var t = 0, e = ["ms", "moz", "webkit", "o"], i = 0; i < e.length && !window.requestAnimationFrame; ++i) window.requestAnimationFrame = window[e[i] + "RequestAnimationFrame"], window.cancelAnimationFrame = window[e[i] + "CancelAnimationFrame"] || window[e[i] + "CancelRequestAnimationFrame"];
        window.requestAnimationFrame || (window.requestAnimationFrame = function(e) {
            var i = (new Date).getTime(),
                n = Math.max(0, 16 - (i - t)),
                s = window.setTimeout(function() {
                    e(i + n)
                }, n);
            return t = i + n, s
        }), window.cancelAnimationFrame || (window.cancelAnimationFrame = function(t) {
            clearTimeout(t)
        })
    }(),

    // Particle JS (.large-header) change as you need....:)
    function() {
        function t() {
            u = window.innerWidth, c = window.innerHeight, g = {
                x: u / 2,
                y: c / 2
            }, p = document.getElementById("large-header"), p.style.height = c + "px", f = document.getElementById("demo-canvas"), f.width = u, f.height = c, m = f.getContext("2d"), d = [];
            for (var t = 0; u > t; t += u / 20)
                for (var e = 0; c > e; e += c / 20) {
                    var i = t + Math.random() * u / 20,
                        n = e + Math.random() * c / 20,
                        s = {
                            x: i,
                            originX: i,
                            y: n,
                            originY: n
                        };
                    d.push(s)
                }
            for (var r = 0; r < d.length; r++) {
                for (var a = [], o = d[r], h = 0; h < d.length; h++) {
                    var v = d[h];
                    if (o != v) {
                        for (var w = !1, y = 0; 5 > y; y++) w || void 0 == a[y] && (a[y] = v, w = !0);
                        for (var y = 0; 5 > y; y++) w || _(o, v) < _(o, a[y]) && (a[y] = v, w = !0)
                    }
                }
                o.closest = a
            }
            for (var r in d) {
                var T = new l(d[r], 2 + 2 * Math.random(), "rgba(255,255,255,0.3)");
                d[r].circle = T
            }
        }

        function e() {
            "ontouchstart" in window || window.addEventListener("mousemove", i), window.addEventListener("scroll", n), window.addEventListener("resize", s)
        }

        function i(t) {
            var e = posy = 0;
            t.pageX || t.pageY ? (e = t.pageX, posy = t.pageY) : (t.clientX || t.clientY) && (e = t.clientX + document.body.scrollLeft + document.documentElement.scrollLeft, posy = t.clientY + document.body.scrollTop + document.documentElement.scrollTop), g.x = e, g.y = posy
        }

        function n() {
            v = document.body.scrollTop > c ? !1 : !0
        }

        function s() {
            u = window.innerWidth, c = window.innerHeight, p.style.height = c + "px", f.width = u, f.height = c
        }

        function r() {
            a();
            for (var t in d) o(d[t])
        }

        function a() {
            if (v) {
                m.clearRect(0, 0, u, c);
                for (var t in d) Math.abs(_(g, d[t])) < 4e3 ? (d[t].active = .3, d[t].circle.active = .6) : Math.abs(_(g, d[t])) < 2e4 ? (d[t].active = .1, d[t].circle.active = .3) : Math.abs(_(g, d[t])) < 4e4 ? (d[t].active = .02, d[t].circle.active = .1) : (d[t].active = 0, d[t].circle.active = 0), h(d[t]), d[t].circle.draw()
            }
            requestAnimationFrame(a)
        }

        function o(t) {
            TweenLite.to(t, 1 + 1 * Math.random(), {
                x: t.originX - 50 + 100 * Math.random(),
                y: t.originY - 50 + 100 * Math.random(),
                ease: Circ.easeInOut,
                onComplete: function() {
                    o(t)
                }
            })
        }

        // Strokestyle color change as you need ...:)
        function h(t) {
            if (t.active)
                for (var e in t.closest) m.beginPath(), m.moveTo(t.x, t.y), m.lineTo(t.closest[e].x, t.closest[e].y), m.strokeStyle = "rgba(255,255,255," + t.active + ")", m.stroke()
        }

        // Strokestyle color change as you need ...:)
        function l(t, e, i) {
            var n = this;
            ! function() {
                n.pos = t || null, n.radius = e || null, n.color = i || null
            }(), this.draw = function() {
                n.active && (m.beginPath(), m.arc(n.pos.x, n.pos.y, n.radius, 0, 2 * Math.PI, !1), m.fillStyle = "rgba(255,255,255," + n.active + ")", m.fill())
            }
        }

        function _(t, e) {
            return Math.pow(t.x - e.x, 2) + Math.pow(t.y - e.y, 2)
        }
        var u, c, p, f, m, d, g, v = !0;
        t(), r(), e()
    }();