<template>
  <div class="checkout-page">
    <h2 class="page-title">确认订单</h2>

    <div class="card">
      <section class="section">
        <h3 class="section-title">收货地址</h3>

        <div class="addr-list" v-if="addresses.length">
          <button v-for="addr in addresses" :key="addr.id" class="addr"
            :class="{ on: selectedAddrId === addr.id }" @click="selectAddr(addr)">
            <span class="addr-mark" aria-hidden="true"></span>
            <span class="addr-text">{{ addr.address }}</span>
            <span class="addr-badge" v-if="addr.isDefault === 1">默认</span>
          </button>
          <button class="addr" :class="{ on: selectedAddrId === -1 }" @click="selectCustom">
            <span class="addr-mark" aria-hidden="true"></span>
            <span class="addr-text">使用新地址</span>
          </button>
        </div>

        <el-input v-if="selectedAddrId === -1 || !addresses.length" v-model="customAddress" type="textarea"
          :rows="2" placeholder="请输入收货人姓名、电话及详细地址" class="addr-input" />
      </section>

      <section class="section">
        <h3 class="section-title">商品清单</h3>
        <div class="item-row" v-for="item in items" :key="item.id">
          <span class="item-name">{{ item.title }} <em>x{{ item.quantity }}</em></span>
          <span class="item-sub">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </section>

      <div class="total">
        <span>应付金额</span>
        <span class="final">&yen;{{ total.toFixed(2) }}</span>
      </div>

      <button class="pill pay" @click="handlePay" :disabled="paying">
        {{ paying ? '支付中…' : '立即支付' }}
      </button>
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
  const def = addresses.value.find(a => a.isDefault === 1)
  if (def) selectedAddrId.value = def.id
  else if (addresses.value.length) selectedAddrId.value = addresses.value[0].id
  else selectedAddrId.value = -1
})
</script>

<style scoped>
.checkout-page {
  max-width: 680px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}
.card {
  margin-top: 24px;
  padding: 32px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}

.section { margin-bottom: 32px; }
.section-title {
  margin-bottom: 12px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.addr {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  margin-bottom: 8px;
  padding: 14px 16px;
  text-align: left;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.addr:hover { border-color: var(--primary); }
.addr.on {
  border: 2px solid var(--primary-focus);
  padding: 13px 15px;
}
.addr-mark {
  position: relative;
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  border: 1px solid var(--ink-muted-48);
  border-radius: 50%;
}
.addr.on .addr-mark { border-color: var(--primary); }
.addr.on .addr-mark::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: var(--primary);
}
.addr-text { flex: 1; }
.addr-badge {
  flex-shrink: 0;
  padding: 2px 10px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.addr-input { margin-top: 12px; }

.item-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.item-name em {
  font-style: normal;
  margin-left: 6px;
  color: var(--ink-muted-80);
}
.item-sub { font-weight: 600; white-space: nowrap; }

.total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}
.final {
  font-size: 32px;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.pay {
  width: 100%;
  height: 50px;
  margin-top: 24px;
}
</style>
