/**
 * https://www.khronos.org/files/webgl/webgl-reference-card-1_0.pdf
 */

@file:Suppress("unused")

package com.soywiz.korag.shader

import com.soywiz.kmem.*
import com.soywiz.korio.lang.*

enum class VarKind(val bytesSize: Int) {
	BYTE(1), UNSIGNED_BYTE(1), SHORT(2), UNSIGNED_SHORT(2), INT(4), FLOAT(4)
}

enum class VarType(val kind: VarKind, val elementCount: Int, val isMatrix: Boolean = false) {
	VOID(VarKind.BYTE, elementCount = 0),

	Mat2(VarKind.FLOAT, elementCount = 4, isMatrix = true),
	Mat3(VarKind.FLOAT, elementCount = 9, isMatrix = true),
	Mat4(VarKind.FLOAT, elementCount = 16, isMatrix = true),

	TextureUnit(VarKind.INT, elementCount = 1),

	Int1(VarKind.INT, elementCount = 1),

	Float1(VarKind.FLOAT, elementCount = 1),
	Float2(VarKind.FLOAT, elementCount = 2),
	Float3(VarKind.FLOAT, elementCount = 3),
	Float4(VarKind.FLOAT, elementCount = 4),

	Short1(VarKind.SHORT, elementCount = 1),
	Short2(VarKind.SHORT, elementCount = 2),
	Short3(VarKind.SHORT, elementCount = 3),
	Short4(VarKind.SHORT, elementCount = 4),

	Bool1(VarKind.UNSIGNED_BYTE, elementCount = 1),

	Byte4(VarKind.UNSIGNED_BYTE, elementCount = 4), // OLD: Is this right?

	SByte1(VarKind.BYTE, elementCount = 1),
	SByte2(VarKind.BYTE, elementCount = 2),
	SByte3(VarKind.BYTE, elementCount = 3),
	SByte4(VarKind.BYTE, elementCount = 4),

	UByte1(VarKind.UNSIGNED_BYTE, elementCount = 1),
	UByte2(VarKind.UNSIGNED_BYTE, elementCount = 2),
	UByte3(VarKind.UNSIGNED_BYTE, elementCount = 3),
	UByte4(VarKind.UNSIGNED_BYTE, elementCount = 4),

	SShort1(VarKind.SHORT, elementCount = 1),
	SShort2(VarKind.SHORT, elementCount = 2),
	SShort3(VarKind.SHORT, elementCount = 3),
	SShort4(VarKind.SHORT, elementCount = 4),

	UShort1(VarKind.UNSIGNED_SHORT, elementCount = 1),
	UShort2(VarKind.UNSIGNED_SHORT, elementCount = 2),
	UShort3(VarKind.UNSIGNED_SHORT, elementCount = 3),
	UShort4(VarKind.UNSIGNED_SHORT, elementCount = 4),

	SInt1(VarKind.INT, elementCount = 1),
	SInt2(VarKind.INT, elementCount = 2),
	SInt3(VarKind.INT, elementCount = 3),
	SInt4(VarKind.INT, elementCount = 4),
	;

	val bytesSize: Int = kind.bytesSize * elementCount

	companion object {
		fun BYTE(count: Int) =
			when (count) { 0 -> VOID; 1 -> SByte1; 2 -> SByte2; 3 -> SByte3; 4 -> SByte4; else -> invalidOp; }

		fun UBYTE(count: Int) =
			when (count) { 0 -> VOID; 1 -> UByte1; 2 -> UByte2; 3 -> UByte3; 4 -> UByte4; else -> invalidOp; }

		fun SHORT(count: Int) =
			when (count) { 0 -> VOID; 1 -> SShort1; 2 -> SShort2; 3 -> SShort3; 4 -> SShort4; else -> invalidOp; }

		fun USHORT(count: Int) =
			when (count) { 0 -> VOID; 1 -> UShort1; 2 -> UShort2; 3 -> UShort3; 4 -> UShort4; else -> invalidOp; }

		fun INT(count: Int) =
			when (count) { 0 -> VOID; 1 -> SInt1; 2 -> SInt2; 3 -> SInt3; 4 -> SInt4; else -> invalidOp; }

		fun FLOAT(count: Int) =
			when (count) { 0 -> VOID; 1 -> Float1; 2 -> Float2; 3 -> Float3; 4 -> Float4; else -> invalidOp; }
	}

}

//val out_Position = Output("gl_Position", VarType.Float4)
//val out_FragColor = Output("gl_FragColor", VarType.Float4)

enum class ShaderType {
	VERTEX, FRAGMENT
}

open class Operand(val type: VarType) {
}

open class Variable(val name: String, type: VarType) : Operand(type) {
	var id: Int = 0
	var data: Any? = null
}

open class Attribute(
	name: String,
	type: VarType,
	val normalized: Boolean,
	val offset: Int? = null,
	val active: Boolean = true
) : Variable(name, type) {
	constructor(name: String, type: VarType, normalized: Boolean) : this(name, type, normalized, null, true)

	fun inactived() = Attribute(name, type, normalized, offset = null, active = false)
	override fun toString(): String = "Attribute($name)"
}

open class Varying(name: String, type: VarType) : Variable(name, type) {
	override fun toString(): String = "Varying($name)"
}

open class Uniform(name: String, type: VarType) : Variable(name, type) {
	//companion object {
	//	var lastUid = 0
	//}
	//
	//val uid = lastUid++
	override fun toString(): String = "Uniform($name)"
}

open class Temp(id: Int, type: VarType) : Variable("temp$id", type) {
	override fun toString(): String = "Temp($name)"
}

object Output : Variable("out", VarType.Float4) {
	override fun toString(): String = "Output"
}

class Program(val vertex: VertexShader, val fragment: FragmentShader, val name: String = "program") : Closeable {
	val uniforms by lazy { vertex.uniforms + fragment.uniforms }
	val attributes by lazy { vertex.attributes + fragment.attributes }

	override fun close() {
	}

	override fun toString(): String =
		"Program(name=$name, attributes=${attributes.map { it.name }}, uniforms=${uniforms.map { it.name }})"

	class Binop(val left: Operand, val op: String, val right: Operand) : Operand(left.type)
	class IntLiteral(val value: Int) : Operand(VarType.Int1)
	class FloatLiteral(val value: Float) : Operand(VarType.Float1)
	class BoolLiteral(val value: Boolean) : Operand(VarType.Bool1)
	class Vector(type: VarType, val ops: Array<Operand>) : Operand(type)
	class Swizzle(val left: Operand, val swizzle: String) : Operand(left.type)
	class ArrayAccess(val left: Operand, val index: Operand) : Operand(left.type)

	class Func(val name: String, val ops: List<Operand>) : Operand(VarType.Float1) {
		constructor(name: String, vararg ops: Operand) : this(name, ops.toList())
	}

	sealed class Stm {
		class Stms(val stms: List<Stm>) : Stm()
		class Set(val to: Operand, val from: Operand) : Stm()
		class Discard : Stm()
		class If(val cond: Operand, val tbody: Stm, var fbody: Stm? = null) : Stm()
	}

	// http://mew.cx/glsl_quickref.pdf
	class Builder(val type: ShaderType) {
		val outputStms = arrayListOf<Stm>()

		//inner class BuildIf(val stmIf: Stm.If) {
		//	fun ELSEIF(cond: Operand, callback: Builder.() -> Unit): BuildIf {
		//		//val body = Builder(type)
		//		//body.callback()
		//		//outputStms += Stm.If(cond, Stm.Stms(body.outputStms))
		//		TODO()
		//	}
//
		//	infix fun ELSE(callback: Builder.() -> Unit) {
		//		//val body = Builder(type)
		//		//body.callback()
		//		//outputStms += Stm.If(cond, Stm.Stms(body.outputStms))
		//		TODO()
		//	}
		//}

		infix fun Stm.If.ELSE(callback: Builder.() -> Unit) {
			val body = Builder(type)
			body.callback()
			this.fbody = Stm.Stms(body.outputStms)
		}

		inline fun IF(cond: Operand, callback: Builder.() -> Unit): Stm.If {
			val body = Builder(type)
			body.callback()
			val stmIf = Stm.If(cond, Stm.Stms(body.outputStms))
			outputStms += stmIf
			return stmIf
		}

		fun SET(target: Operand, expr: Operand) {
			outputStms += Stm.Set(target, expr)
		}

		fun DISCARD() {
			outputStms += Stm.Discard()
		}

		private var tempLastId = 3
		fun createTemp(type: VarType) = Temp(tempLastId++, type)

		infix fun Operand.set(from: Operand) = run { outputStms += Stm.Set(this, from) }
		infix fun Operand.setTo(from: Operand) = run { outputStms += Stm.Set(this, from) }

		fun Operand.assign(from: Operand) {
			outputStms += Stm.Set(this, from)
		}

		//infix fun Operand.set(to: Operand) = Stm.Set(this, to)
		val out: Output = Output
		//fun out(to: Operand) = Stm.Set(if (type == ShaderType.VERTEX) out_Position else out_FragColor, to)

		fun sin(arg: Operand) = Func("sin", arg)
		fun cos(arg: Operand) = Func("cos", arg)
		fun tan(arg: Operand) = Func("tan", arg)

		fun asin(arg: Operand) = Func("asin", arg)
		fun acos(arg: Operand) = Func("acos", arg)
		fun atan(arg: Operand) = Func("atan", arg)

		fun radians(arg: Operand) = Func("radians", arg)
		fun degrees(arg: Operand) = Func("degrees", arg)

		// Sampling
		fun texture2D(a: Operand, b: Operand) = Func("texture2D", a, b)

		fun func(name: String, vararg args: Operand) = Func(name, *args.map { it }.toTypedArray())

		fun pow(b: Operand, e: Operand) = Func("pow", b, e)
		fun exp(v: Operand) = Func("exp", v)
		fun exp2(v: Operand) = Func("exp2", v)
		fun log(v: Operand) = Func("log", v)
		fun log2(v: Operand) = Func("log2", v)
		fun sqrt(v: Operand) = Func("sqrt", v)
		fun inversesqrt(v: Operand) = Func("inversesqrt", v)

		fun abs(v: Operand) = Func("abs", v)
		fun sign(v: Operand) = Func("sign", v)
		fun ceil(v: Operand) = Func("ceil", v)
		fun floor(v: Operand) = Func("floor", v)
		fun fract(v: Operand) = Func("fract", v)
		fun clamp(v: Operand, min: Operand, max: Operand) = Func("clamp", v, min, max)
		fun min(a: Operand, b: Operand) = Func("min", a, b)
		fun max(a: Operand, b: Operand) = Func("max", a, b)
		fun mod(a: Operand, b: Operand) = Func("mod", a, b)
		fun mix(a: Operand, b: Operand, step: Operand) = Func("mix", a, b, step)
		fun step(a: Operand, b: Operand) = Func("step", a, b)
		fun smoothstep(a: Operand, b: Operand, c: Operand) = Func("smoothstep", a, b, c)

		fun length(a: Operand) = Func("length", a)
		fun distance(a: Operand, b: Operand) = Func("distance", a, b)
		fun dot(a: Operand, b: Operand) = Func("dot", a, b)
		fun cross(a: Operand, b: Operand) = Func("cross", a, b)
		fun normalize(a: Operand) = Func("normalize", a)
		fun faceforward(a: Operand, b: Operand, c: Operand) = Func("faceforward", a, b, c)
		fun reflect(a: Operand, b: Operand) = Func("reflect", a, b)
		fun refract(a: Operand, b: Operand, c: Operand) = Func("refract", a, b, c)

		val Int.lit: IntLiteral get() = IntLiteral(this)
		val Double.lit: FloatLiteral get() = FloatLiteral(this.toFloat())
		val Float.lit: FloatLiteral get() = FloatLiteral(this)
		val Boolean.lit: BoolLiteral get() = BoolLiteral(this)
        //val Number.lit: Operand get() = this // @TODO: With Kotlin.JS you cannot differentiate Int, Float, Double with 'is'. Since it generates typeof $receiver === 'number' for all of them
		fun lit(type: VarType, vararg ops: Operand): Operand = Vector(type, ops as Array<Operand>)
		fun vec1(vararg ops: Operand): Operand = Vector(VarType.Float1, ops as Array<Operand>)
		fun vec2(vararg ops: Operand): Operand = Vector(VarType.Float2, ops as Array<Operand>)
		fun vec3(vararg ops: Operand): Operand = Vector(VarType.Float3, ops as Array<Operand>)
		fun vec4(vararg ops: Operand): Operand = Vector(VarType.Float4, ops as Array<Operand>)
		//fun Operand.swizzle(swizzle: String): Operand = Swizzle(this, swizzle)
		operator fun Operand.get(index: Int): Operand {
			return when {
				this.type.isMatrix -> ArrayAccess(this, index.lit)
				else -> when (index) {
					0 -> this.x
					1 -> this.y
					2 -> this.z
					3 -> this.w
					else -> error("Invalid index $index")
				}
			}
		}
		operator fun Operand.get(swizzle: String) = Swizzle(this, swizzle)
		val Operand.x get() = this["x"]
		val Operand.y get() = this["y"]
		val Operand.z get() = this["z"]
		val Operand.w get() = this["w"]

		val Operand.r get() = this["x"]
		val Operand.g get() = this["y"]
		val Operand.b get() = this["z"]
		val Operand.a get() = this["w"]

		operator fun Operand.unaryMinus() = 0.0.lit - this

		operator fun Operand.minus(that: Operand) = Binop(this, "-", that)
		operator fun Operand.plus(that: Operand) = Binop(this, "+", that)
		operator fun Operand.times(that: Operand) = Binop(this, "*", that)
		operator fun Operand.div(that: Operand) = Binop(this, "/", that)
		operator fun Operand.rem(that: Operand) = Binop(this, "%", that)

		infix fun Operand.eq(that: Operand) = Binop(this, "==", that)
		infix fun Operand.ne(that: Operand) = Binop(this, "!=", that)
		infix fun Operand.lt(that: Operand) = Binop(this, "<", that)
		infix fun Operand.le(that: Operand) = Binop(this, "<=", that)
		infix fun Operand.gt(that: Operand) = Binop(this, ">", that)
		infix fun Operand.ge(that: Operand) = Binop(this, ">=", that)
	}

	open class Visitor<E>(val default: E) {
		open fun visit(stm: Stm) = when (stm) {
			is Stm.Stms -> visit(stm)
			is Stm.Set -> visit(stm)
			is Stm.If -> visit(stm)
			is Stm.Discard -> visit(stm)
		}

		open fun visit(stms: Stm.Stms) {
			for (stm in stms.stms) visit(stm)
		}

		open fun visit(stm: Stm.If) {
			visit(stm.cond)
			visit(stm.tbody)
		}

		open fun visit(stm: Stm.Set) {
			visit(stm.from)
			visit(stm.to)
		}

		open fun visit(stm: Stm.Discard) {
		}

		open fun visit(operand: Operand): E = when (operand) {
			is Variable -> visit(operand)
			is Binop -> visit(operand)
			is BoolLiteral -> visit(operand)
			is IntLiteral -> visit(operand)
			is FloatLiteral -> visit(operand)
			is Vector -> visit(operand)
			is Swizzle -> visit(operand)
			is ArrayAccess -> visit(operand)
			is Func -> visit(operand)
			else -> invalidOp("Don't know how to visit operand $operand")
		}

		open fun visit(func: Func): E {
			for (op in func.ops) visit(op)
			return default
		}

		open fun visit(operand: Variable): E = when (operand) {
			is Attribute -> visit(operand)
			is Varying -> visit(operand)
			is Uniform -> visit(operand)
			is Output -> visit(operand)
			is Temp -> visit(operand)
			else -> invalidOp("Don't know how to visit basename $operand")
		}

		open fun visit(temp: Temp): E = default
		open fun visit(attribute: Attribute): E = default
		open fun visit(varying: Varying): E = default
		open fun visit(uniform: Uniform): E = default
		open fun visit(output: Output): E = default
		open fun visit(operand: Binop): E {
			visit(operand.left)
			visit(operand.right)
			return default
		}

		open fun visit(operand: Swizzle): E {
			visit(operand.left)
			return default
		}

		open fun visit(operand: ArrayAccess): E {
			visit(operand.left)
			visit(operand.index)
			return default
		}

		open fun visit(operand: Vector): E {
			for (op in operand.ops) visit(op)
			return default
		}

		open fun visit(operand: IntLiteral): E = default
		open fun visit(operand: FloatLiteral): E = default
		open fun visit(operand: BoolLiteral): E = default
	}
}

open class Shader(val type: ShaderType, val stm: Program.Stm) {
	val uniforms by lazy {
		val out = LinkedHashSet<Uniform>()
		object : Program.Visitor<Unit>(Unit) {
			override fun visit(uniform: Uniform) = run { out += uniform }
		}.visit(stm)
		out.toSet()
	}

	val attributes by lazy {
		val out = LinkedHashSet<Attribute>()
		object : Program.Visitor<Unit>(Unit) {
			override fun visit(attribute: Attribute) = run { out += attribute }
		}.visit(stm)
		out.toSet()
	}
}

open class VertexShader(stm: Program.Stm) : Shader(ShaderType.VERTEX, stm)
open class FragmentShader(stm: Program.Stm) : Shader(ShaderType.FRAGMENT, stm)

fun FragmentShader.appending(callback: Program.Builder.() -> Unit): FragmentShader {
	return FragmentShader(Program.Stm.Stms(listOf(this.stm, FragmentShader(callback).stm)))
}

fun VertexShader(callback: Program.Builder.() -> Unit): VertexShader {
	val builder = Program.Builder(ShaderType.VERTEX)
	builder.callback()
	return VertexShader(Program.Stm.Stms(builder.outputStms))
}

fun FragmentShader(callback: Program.Builder.() -> Unit): FragmentShader {
	val builder = Program.Builder(ShaderType.FRAGMENT)
	builder.callback()
	return FragmentShader(Program.Stm.Stms(builder.outputStms))
}

class VertexLayout(attr: List<Attribute>, private val layoutSize: Int?) {
	private val myattr = attr
	val attributes = attr
	constructor(attributes: List<Attribute>) : this(attributes, null)
	constructor(vararg attributes: Attribute) : this(attributes.toList(), null)
	constructor(vararg attributes: Attribute, layoutSize: Int? = null) : this(attributes.toList(), layoutSize)

	private var _lastPos: Int = 0

	val alignments = myattr.map {
		val a = it.type.kind.bytesSize
		if (a <= 1) 1 else a
	}

	val attributePositions = myattr.map {
		if (it.offset != null) {
			_lastPos = it.offset
		} else {
			_lastPos = _lastPos.nextAlignedTo(it.type.kind.bytesSize)
		}
		val out = _lastPos
		_lastPos += it.type.bytesSize
		out
	}

	val maxAlignment = alignments.max() ?: 1
	val totalSize: Int = run { layoutSize ?: _lastPos.nextAlignedTo(maxAlignment) }

	override fun toString(): String = "VertexLayout[${myattr.map { it.name }.joinToString(", ")}]"
}
