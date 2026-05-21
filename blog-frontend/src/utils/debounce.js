/**
 * 创建一个防抖函数，在连续调用时只执行最后一次。
 * 适用场景：搜索输入、自动保存、窗口 resize。
 *
 * @param {Function} fn  要执行的函数
 * @param {number} delay 延迟毫秒数
 * @returns {Function} 包装后的防抖函数
 */
export function debounce(fn, delay = 30000) {
  let timer = null
  const debounced = (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
      timer = null
    }, delay)
  }
  debounced.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }
  debounced.flush = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
      fn()
    }
  }
  return debounced
}
