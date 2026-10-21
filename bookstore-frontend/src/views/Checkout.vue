<template>
  <div class="checkout-page">
    <h2>确认订单</h2>

    <div class="checkout-card">
      <div class="section">
        <h3><el-icon><Location /></el-icon> 收货地址</h3>

        <div class="addr-list" v-if="addresses.length">
          <div v-for="addr in addresses" :key="addr.id" class="addr-option" :class="{ selected: selectedAddrId === addr.id }"
            @click="selectAddr(addr)">
            <div class="addr-radio">
              <el-icon v-if="selectedAddrId === addr.id"><CircleCheck /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
            </div>
            <div class="addr-content">
              <div class="addr-text">{{ addr.address }}</div>
              <el-tag v-if="addr.isDefault === 1" type="danger" size="small">默认</el-tag>
            </div>
          </div>
          <div class="addr-option" :class="{ selected: selectedAddrId === -1 }" @click="selectCustom">
            <div class="addr-radio">
              <el-icon v-if="selectedAddrId === -1"><CircleCheck /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
            </div>
            <div class="addr-content">
              <span class="addr-text custom-label">使用新地址</span>
            </div>
          </div>
        </div>

        <el-input v-if="selectedAddrId === -1 || !addresses.length" v-model="customAddress" type="textarea" :rows="2"
          placeholder="请输入收货人姓名、电话及详细地址" size="large" class="addr-input" />
      </div>

      <div class="section">
        <h3><el-icon><List /></el-icon> 商品清单</h3>
        <div class="item-row" v-for="item in items" :key="item.id">
          <span class="item-name">{{ item.title }} <em>x{{ item.quantity }}</em></span>
          <span class="item-sub">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </div>

      <div class="total-section">
        <span>应付金额</span>
        <span class="final-price">&yen;{{ total.toFixed(2) }}</span>
      </div>

      <el-button type="primary" size="large" @click="handlePay" :loading="paying" class="pay-btn">
        <el-icon><Money /></el-icon> 立即支付
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart } from '../api/cart'
import { placeOrder } from '../api/order'
import { useCartStore } from '../stores/cart'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const items = ref([])
const addresses = ref([])
const selectedAddrId = ref(null)
const customAddress = ref('')
const paying = ref(false)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

const finalAddress = computed(() => {
  if (selectedAddrId.value === -1) return customAddress.value
  if (selectedAddrId.value) {
    const addr = addresses.value.find(a => a.id === selectedAddrId.value)
    return addr ? addr.address : ''
  }
  return customAddress.value
})

function selectAddr(addr) {
  selectedAddrId.value = addr.id
}
function selectCustom() {
  selectedAddrId.value = -1
  customAddress.value = ''
}

async function handlePay() {
  const addr = finalAddress.value
  if (!addr.trim()) { ElMessage.warning('请选择或填写收货地址'); return }
  paying.value = true
  try {
    await placeOrder({ address: addr })
    cartStore.fetchCount()
    ElMessage.success('支付成功！')
    router.push('/orders')
  } finally { paying.value = false }
}

onMounted(async () => {
  const [cartRes, addrRes] = await Promise.all([getCart(), api.get('/user/addresses')])
  items.value = cartRes.data
  addresses.value = addrRes.data
  if (!items.value.length) { ElMessage.warning('购物车为空'); router.push('/cart') }
  // 自动选中默认地址
  const def = addresses.value.find(a => a.isDefault === 1)
  if (def) selectedAddrId.value = def.id
  else if (addresses.value.length) selectedAddrId.value = addresses.value[0].id
  else selectedAddrId.value = -1
})
</script>

<style scoped>
.checkout-page { max-width: 680px; margin: 0 auto; }
.checkout-page h2 { font-size: 22px; font-weight: 700; margin-bottom: 24px; }
.checkout-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); padding: 32px; }

.section { margin-bottom: 28px; }
.section h3 { font-size: 15px; font-weight: 600; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }

.addr-list { margin-bottom: 12px; }
.addr-option {
  display: flex; align-items: flex-start; gap: 12px; padding: 14px 16px;
  border: 1.5px solid var(--border); border-radius: 8px; margin-bottom: 8px;
  cursor: pointer; transition: all 0.2s;
}
.addr-option:hover { border-color: var(--primary); }
.addr-option.selected { border-color: var(--primary); background: #f0f5ff; }
.addr-radio {
  font-size: 20px; color: var(--border); padding-top: 1px;
  transition: color 0.2s;
}
.addr-option.selected .addr-radio { color: var(--primary); }
.addr-content { display: flex; align-items: center; gap: 8px; flex: 1; }
.addr-text { font-size: 14px; line-height: 1.5; }
.custom-label { color: var(--text-secondary); }
.addr-input { margin-top: 12px; }

.item-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f3f4f6; font-size: 14px; }
.item-name em { font-style: normal; color: var(--text-secondary); margin-left: 4px; }
.item-sub { font-weight: 600; }

.total-section {
  display: flex; justify-content: flex-end; align-items: baseline; gap: 12px;
  padding: 20px 0; border-top: 2px solid var(--border); margin-bottom: 20px;
  font-size: 16px;
}
.final-price { font-size: 32px; font-weight: 800; color: var(--danger); }

.pay-btn { width: 100%; height: 50px; font-size: 17px; border-radius: 10px; font-weight: 600; }
</style>
