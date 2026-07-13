<template>
  <div class="bg" aria-hidden="true">
    <canvas ref="canvasRef" class="bg-canvas"></canvas>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
const canvasRef = ref(null)
let af = null, pts = []

class DP {
  constructor(w,h) {
    this.x=Math.random()*w; this.y=Math.random()*h
    this.size=Math.random()*1.5+0.3
    this.vx=(Math.random()-0.5)*0.15; this.vy=(Math.random()-0.5)*0.15
    this.op=Math.random()*0.20+0.03
  }
  update(w,h) { this.x+=this.vx; this.y+=this.vy; if(this.x<-10)this.x=w+10; if(this.x>w+10)this.x=-10; if(this.y<-10)this.y=h+10; if(this.y>h+10)this.y=-10 }
  draw(ctx) { ctx.beginPath(); ctx.arc(this.x,this.y,this.size,0,Math.PI*2); ctx.fillStyle=`rgba(108,131,255,${this.op})`; ctx.fill() }
}

onMounted(() => {
  const c=canvasRef.value; if(!c)return; const ctx=c.getContext('2d')
  const rs=()=>{c.width=window.innerWidth;c.height=window.innerHeight}
  rs();window.addEventListener('resize',rs)
  pts=Array.from({length:50},()=>new DP(c.width,c.height))
  const an=()=>{ctx.clearRect(0,0,c.width,c.height);pts.forEach(p=>{p.update(c.width,c.height);p.draw(ctx)});af=requestAnimationFrame(an)}
  af=requestAnimationFrame(an)
  onBeforeUnmount(()=>{cancelAnimationFrame(af);window.removeEventListener('resize',rs)})
})
</script>

<style scoped>
.bg { position:fixed; inset:0; pointer-events:none; z-index:0; overflow:hidden; }
.bg-canvas { position:absolute; inset:0; width:100%; height:100%; }
</style>
