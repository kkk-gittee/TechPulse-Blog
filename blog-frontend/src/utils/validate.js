/**
 * 轻量级表单校验工具，提供类 Zod 的声明式校验 API。
 * 不引入外部依赖，代码体积 < 2KB。
 *
 * 用法：
 *   const schema = z.object({
 *     username: z.string().min(3, '用户名至少3个字符').max(20).noSpecial(),
 *     password: z.string().min(6, '密码至少6个字符').max(32),
 *   })
 *   const result = schema.safeParse(formData)
 *   if (!result.success) console.log(result.errors)
 */

class StringValidator {
  constructor() {
    this._checks = []
    this._optional = false
  }

  min(len, msg) { this._checks.push(v => v.length >= len ? null : msg); return this }
  max(len, msg) { this._checks.push(v => v.length <= len ? null : msg || `最多${len}个字符`); return this }
  noSpecial(msg) {
    this._checks.push(v => /^[a-zA-Z0-9_一-龥]+$/.test(v) ? null : msg || '不允许特殊字符')
    return this
  }
  email(msg) {
    this._checks.push(v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? null : msg || '邮箱格式不正确')
    return this
  }

  validate(value) {
    if (!value && this._optional) return null
    for (const check of this._checks) {
      const error = check(value || '')
      if (error) return error
    }
    return null
  }

  optional() { this._optional = true; return this }
}

class ObjectValidator {
  constructor(shape) {
    this._shape = shape
  }

  safeParse(data) {
    const errors = {}
    for (const [key, validator] of Object.entries(this._shape)) {
      const error = validator.validate(data[key])
      if (error) errors[key] = error
    }
    return {
      success: Object.keys(errors).length === 0,
      errors
    }
  }
}

export const z = {
  string: () => new StringValidator(),
  object: (shape) => new ObjectValidator(shape)
}
