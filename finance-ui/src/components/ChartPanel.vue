<template>
  <section class="chart-panel">
    <div class="chart-wrap">
      <div ref="chartRef" class="chart"></div>
      <div class="center">
        <span class="cv">{{ fmt(total) }}</span>
        <span class="cl">{{ t.totalSpent }}</span>
      </div>
    </div>
    <div class="legend">
      <div v-for="(item,i) in legendData" :key="i" class="leg-row">
        <span class="leg-dot" :style="{background:item.color}"></span>
        <span class="leg-name">{{ item.name }}</span>
        <span class="leg-val">{{ fmt(item.value) }}</span>
      </div>
      <div v-if="legendData.length===0" class="leg-empty">{{ t.noTransactions }}</div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { useI18n } from '../i18n/locale'
const props = defineProps({ chartData:{type:Array,default:()=>[]} })
const { language, t } = useI18n()
const chartRef = ref(null)
let inst = null
const colors = ['#6c83ff','#00e89d','#f5a623','#ff5c72','#06B6D4','#8B5CF6','#10B981','#F97316']
const total = computed(()=>props.chartData.reduce((s,i)=>s+(Number(i.amount)||0),0))
const legendData = computed(()=>props.chartData.map((item,i)=>({name:item.category_name||item.name,value:Number(item.amount)||0,color:colors[i%colors.length]})))
const fmt = (v)=>{const n=Number(v)||0;const l=language.value==='zh'?'zh-CN':'en-US';const c=language.value==='zh'?'CNY':'USD';return new Intl.NumberFormat(l,{style:'currency',currency:c,maximumFractionDigits:0}).format(n)}

const render = () => {
  if(!chartRef.value)return; if(!inst)inst=echarts.init(chartRef.value)
  const data=props.chartData.map(item=>({name:item.category_name||item.name,value:Number(item.amount)||0}))
  inst.setOption({series:[{type:'pie',radius:['65%','84%'],center:['50%','50%'],data,padAngle:2,itemStyle:{borderColor:'#0f111a',borderRadius:3,borderWidth:2},color:colors,label:{show:false},emphasis:{scale:true,scaleSize:4,label:{show:false}}}],tooltip:{trigger:'item',backgroundColor:'rgba(16,18,28,0.96)',borderColor:'rgba(255,255,255,0.06)',textStyle:{color:'#e8ecf4',fontSize:13},formatter:'{b}: {c} ({d}%)',extraCssText:'border-radius:10px;box-shadow:0 4px 24px rgba(0,0,0,0.5);'}})
}

onMounted(()=>{render();window.addEventListener('resize',()=>inst?.resize())})
onBeforeUnmount(()=>{inst?.dispose()})
watch([()=>props.chartData,()=>language.value],()=>render(),{deep:true})
</script>

<style scoped>
.chart-panel { display:flex; flex-direction:column; gap:var(--s-3); }
.chart-wrap { position:relative; height:200px; }
.chart { width:100%; height:100%; }
.center { position:absolute; top:50%; left:50%; transform:translate(-50%,-50%); text-align:center; pointer-events:none; }
.cv { display:block; font-size:var(--fs-lg); font-weight:var(--fw-bold); color:var(--text-primary); }
.cl { display:block; font-size:var(--fs-xs); color:var(--text-tertiary); }

.legend { display:flex; flex-direction:column; gap:2px; }
.leg-row { display:flex; align-items:center; gap:var(--s-2); padding:2px 0; }
.leg-dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.leg-name { flex:1; font-size:var(--fs-xs); color:var(--text-secondary); }
.leg-val { font-size:var(--fs-xs); font-weight:var(--fw-medium); color:var(--text-primary); font-variant-numeric:tabular-nums; }
.leg-empty { text-align:center; padding:var(--s-4) 0; color:var(--text-tertiary); font-size:var(--fs-xs); }
</style>
