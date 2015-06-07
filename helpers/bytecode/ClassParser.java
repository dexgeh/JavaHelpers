package helpers.bytecode;

import java.nio.ByteBuffer;

import lombok.Getter;

// taken and restructured from Clazz.java and Parse.java, posted on stackoverflow (linked to google docs) but I lost the link

public class ClassParser {
	@Getter
	private byte[] bytecode;
	@Getter
	private int majorVersion;
	@Getter
	private int minorVersion;
	@Getter
	private ConstantPoolInfo[] constantPool;
	@Getter
	private int accessFlags;
	@Getter
	private int thisClass;
	@Getter
	private int superClass;
	@Getter
	private int[] interfaces;
	@Getter
	private Info[] fields;
	@Getter
	private Info[] methods;
	@Getter
	private Attribute[] attributes;

	private boolean strict;

	public ClassParser(byte[] classBytes, boolean strict) throws Exception {
		this.strict = strict;
		ByteBuffer buffer = ByteBuffer.wrap(classBytes);
		buffer.getInt(); // 0xCAFEBABE
		minorVersion = 0x0000FFFF & buffer.getShort();
		majorVersion = 0x0000FFFF & buffer.getShort();
		constantPool = parseConstantPool(buffer);
		reParseConstantPool();
		accessFlags = 0x0000FFFF & buffer.getShort();
		thisClass = 0x0000FFFF & buffer.getShort();
		superClass = 0x0000FFFF & buffer.getShort();
		interfaces = parseInterfaces(buffer);
		fields = parseInfos(buffer);
		methods = parseInfos(buffer);
		int attributesCount = 0x0000FFFF & buffer.getShort();
		attributes = new Attribute[attributesCount];
		for (int j = 0; j < attributesCount; j++) {
			attributes[j] = new Attribute();
			attributes[j].nameIndex = 0x0000FFFF & buffer.getShort();
			long length = 0x00000000FFFFFFFFL & (long) buffer.getInt();
			attributes[j].info = new byte[(int) length];
			buffer.get(attributes[j].info, 0, (int) length);
		}
		bytecode = classBytes;
	}

	private static int getU2(byte[] bytes, int offset) {
		return 0x0000FFFF & (bytes[offset] << 8) | (0x00FF & bytes[offset+1]);
	}

	private static int getU4(byte[] bytes, int offset) {
		return (getU2(bytes, offset) << 16) | getU2(bytes, offset+2);
	}

	/* constant pool */

	// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4
	// 1: utf8
	// 3: Integer
	// 4: Float
	// 5: Long
	// 6: Double
	// 7: Class
	// 8: String
	// 9: Fieldref
	// 10: Methodref
	// 11: InterfaceMethodref
	// 12: Name and type
	// 15: method handle
	// 16: method type
	// 18: invoke dynamic
	// 256: Dummy
	// 261: LongDummy (256 + Long)
	// 262: DoubleDummy (256 + Double)

	private ConstantPoolInfo[] parseConstantPool(ByteBuffer buffer) throws Exception {
		short count = buffer.getShort();
		ConstantPoolInfo[] constantPool = new ConstantPoolInfo[count];
		constantPool[0] = null; // reserved
		for (int i = 1; i < count; i++) {
			constantPool[i] = new ConstantPoolInfo();
			short tag = (short) (0x00FF & buffer.get());
			constantPool[i].tag = tag;
			int length =
				tag == 7 || tag == 8 || tag == 16
				? 2
				: tag == 3 || tag == 4 || tag == 9 || tag == 10 || tag == 11 || tag == 12 || tag == 18
				? 4
				: tag == 5 || tag == 6
				? 8
				: tag == 15
				? 3
				: tag == 1
				? 0x0000FFFF & buffer.getShort()
				: 0;
			if (length == 0 && tag != 1) {
				if (strict) throw new Exception("Unknown tag: " + tag + " at position " + buffer.position());
				constantPool[i] = null;
				return constantPool;
			}
			if (tag != 1) {
				constantPool[i].info = new byte[length];
				buffer.get(constantPool[i].info, 0, length);
			} else {
				constantPool[i].info = new byte[2 + length];
				constantPool[i].info[0] = (byte) (length >> 8);
				constantPool[i].info[1] = (byte) length;
				buffer.get(constantPool[i].info, 2, length);
			}
			if (tag == 5 || tag == 6) {
				i++;
				constantPool[i] = new ConstantPoolInfo();
				constantPool[i].tag = constantPool[i-1].tag + 256;
			}
		}
		return constantPool;
	}
	private void reParseConstantPool() throws Exception {
		// all utf8 entries are loaded, re-read every entry and dereferences some stuff
		for (int i = 1; i < constantPool.length; i++) {
			ConstantPoolInfo cpInfo = constantPool[i];
			if (cpInfo == null) {
				continue;
			}
			if (cpInfo.tag == 7) {
				cpInfo.className = getUtf8AsString(getU2(cpInfo.info, 0));
			} else if (cpInfo.tag == 12) {
				cpInfo.name = getUtf8AsString(getU2(cpInfo.info, 0));
				cpInfo.descriptor = getUtf8AsString(getU2(cpInfo.info, 2));
			} else if (cpInfo.tag == 8) {
				cpInfo.stringValue = getUtf8AsString(getU2(cpInfo.info, 0));
			} else if (cpInfo.tag == 3 || cpInfo.tag == 4) {
				cpInfo.intValue = getU4(cpInfo.info, 0);
			} else if (cpInfo.tag == 5 || cpInfo.tag == 6) {
				cpInfo.longValue = (((long) getU4(cpInfo.info, 0)) << 32) + (0x00000000FFFFFFFFL & ((long) getU4(cpInfo.info, 4)));
			}
		}
	}

	public static class ConstantPoolInfo {
		@Getter
		private int tag;
		@Getter
		private byte[] info;
		// tag 7
		@Getter
		private String className;
		// tag 12
		@Getter
		private String name;
		@Getter
		private String descriptor;
		// tag 8
		@Getter
		private String stringValue;
		// tag 3,4
		@Getter
		private int intValue;
		// tag 5,6
		@Getter
		private long longValue;
	}

	public String getUtf8AsString(int index) throws Exception {
		if (index < 1 || index > constantPool.length) {
			if (strict) throw new Exception("invalid index "+index);
			return null;
		}
		if (!strict && constantPool[index] == null) return null;
		if (constantPool[index].tag != 1) {
			if (strict) throw new Exception("invalid tag for index "+index +": " + constantPool[index].tag);
			return null;
		}
		return new String(constantPool[index].info, 2, constantPool[index].info.length - 2, "utf-8");
	}

	/* interfaces */
	
	private int[] parseInterfaces(ByteBuffer buffer) {
		int count = 0x0000FFFF & buffer.getShort();
		int[] interfaces = new int[count];
		for (int i = 0; i < count; i++) {
			interfaces[i] = 0x0000FFFF & buffer.getShort();
		}
		return interfaces;
	}

	/* infos are fields and methods */

	private Info[] parseInfos(ByteBuffer buffer) {
		int count = 0x0000FFFF & buffer.getShort();
		Info[] infos = new Info[count];
		for (int i = 0; i < count; i++) {
			infos[i] = new Info();
			infos[i].accessFlags = 0x0000FFFF & buffer.getShort();
			infos[i].nameIndex = 0x0000FFFF & buffer.getShort();
			infos[i].descriptorIndex = 0x0000FFFF & buffer.getShort();
			int attributesCount = 0x0000FFFF & buffer.getShort();
			infos[i].attributes = new Attribute[attributesCount];
			for (int j = 0; j < attributesCount; j++) {
				infos[i].attributes[j] = new Attribute();
				infos[i].attributes[j].nameIndex = 0x0000FFFF & buffer.getShort();
				long length = 0x00000000FFFFFFFFL & (long) buffer.getInt();
				infos[i].attributes[j].info = new byte[(int) length];
				buffer.get(infos[i].attributes[j].info, 0, (int) length);
			}
		}
		return infos;
	}

	public static class Info {
		@Getter
		protected int accessFlags;
		@Getter
		protected int nameIndex;
		@Getter
		protected int descriptorIndex;
		@Getter
		protected Attribute[] attributes;
	}

	public static class Attribute {
		@Getter
		private int nameIndex;
		@Getter
		private byte[] info;
	}

}
