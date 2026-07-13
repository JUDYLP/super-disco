<template>
  <canvas ref="canvasRef" class="particle-canvas" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
const canvasRef = ref(null)
let af = null, pts = [], pid = 0
const IC = {r:0,g:232,b:157}, EC = {r:255,g:92,b:114}

class P {
  constructor(id,dir,color,sx,sy,ex,ey,delay,size) {
    this.id=id;this.dir=dir;this.color=color;this.sx=sx;this.sy=sy;this.ex=ex;this.ey=ey;this.delay=delay;this.size=size
    this.born=performance.now()+delay;this.dur=550;this.fd=220;this.alive=true
    this.rot=(Math.random()-0.5)*Math.PI*4;this.rs=(Math.random()-0.5)*0.02
  }
  update(now) {
    const e=now-this.born; if(e<0)return; const p=Math.min(e/this.dur,1)
    const t=1-Math.pow(1-p,3)
    this.x=this.sx+(this.ex-this.sx)*t; this.y=this.sy+(this.ey-this.sy)*t
    this.sc=this.dir==='in'?0.3+0.7*t:1-0.5*t; this.rot+=this.rs
    this.op=p>=1?Math.max(0,1-(e-this.dur)/this.fd):Math.min(p*3,1)
    if(p>=1&&(e-this.dur)/this.fd>=1)this.alive=false
  }
  draw(ctx) {
    if(this.op<=0)return; const {r,g,b}=this.color,s=this.size
    ctx.save();ctx.translate(this.x,this.y);ctx.rotate(this.rot);ctx.scale(this.sc,this.sc);ctx.globalAlpha=this.op
    ctx.beginPath();ctx.arc(0,0,s*.45,0,Math.PI*2);ctx.strokeStyle=`rgba(${r},${g},${b},0.6)`;ctx.lineWidth=1.2;ctx.stroke()
    ctx.beginPath();ctx.arc(0,0,s*.28,0,Math.PI*2);ctx.fillStyle=`rgba(${r},${g},${b},0.3)`;ctx.fill()
    ctx.fillStyle=`rgba(${r},${g},${b},0.9)`;ctx.font=`600 ${s*.38}px "SF Mono",monospace`;ctx.textAlign='center';ctx.textBaseline='middle';ctx.fillText('$',0,0.5)
    ctx.restore()
  }
}

function anim() { const c=canvasRef.value; if(!c)return; const ctx=c.getContext('2d'),now=performance.now(); ctx.clearRect(0,0,c.width,c.height); pts.forEach(p=>{p.update(now);p.draw(ctx)}); pts=pts.filter(p=>p.alive); if(pts.length>0)af=requestAnimationFrame(anim); else af=null }
function start() { if(!af)af=requestAnimationFrame(anim) }
function spawn(dir,count=5) { const c=canvasRef.value; if(!c)return; const color=dir==='in'?IC:EC; const sz=[6,8,10,8,6]; for(let i=0;i<count;i++){ const s=sz[i%sz.length]; const sx=dir==='in'?Math.random()*c.width*.3:c.width*.4+Math.random()*c.width*.3; const sy=c.height*.5+(Math.random()-.5)*200; const ex=dir==='in'?c.width*.08+Math.random()*80:c.width*.7+Math.random()*80; const ey=60+Math.random()*40; pts.push(new P(pid++,dir,color,sx,sy,ex,ey,i*50,s)) } start() }

function resize() { const c=canvasRef.value; if(!c)return; const dpr=window.devicePixelRatio||1; c.width=window.innerWidth*dpr; c.height=window.innerHeight*dpr; c.style.width=window.innerWidth+'px'; c.style.height=window.innerHeight+'px'; c.getContext('2d').scale(dpr,dpr) }

onMounted(()=>{resize();window.addEventListener('resize',resize)})
onBeforeUnmount(()=>{if(af)cancelAnimationFrame(af);window.removeEventListener('resize',resize)})
defineExpose({ spawnIncome:(n)=>spawn('in',n), spawnExpense:(n)=>spawn('out',n) })
</script>

<style scoped>
.particle-canvas { position:fixed; inset:0; pointer-events:none; z-index:9999; }
</style>
