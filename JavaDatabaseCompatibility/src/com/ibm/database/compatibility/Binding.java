package com.ibm.database.compatibility;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class Binding {
	private Integer index = null;
	private Object value;
	private SqlDataType typeOfValue;

	public Binding(Integer index, Object value, SqlDataType typeOfValue) {
		this.index = index;
		this.value = value;
		this.typeOfValue = typeOfValue;
	}

	public Integer getIndex() {
		return this.index;
	}
	
	protected void setIndex(Integer index) {
		this.index = index;
	}

	public Object getValue() {
		return this.value;
	}

	public SqlDataType getTypeOfValue() {
		if (typeOfValue == null) {
			return SqlDataType.OBJECT;
		} else {
			return this.typeOfValue;
		}
	}

	public void bind(PreparedStatement ps) throws SQLException {
		switch (getTypeOfValue()) {
		case BOOLEAN:
			ps.setBoolean(getIndex(), (Boolean) getValue());
			break;
		case BYTE:
			break;
		case CHAR:
		case VARCHAR:
			ps.setString(getIndex(), (String) getValue());
			break;
		case DOUBLE:
			ps.setDouble(getIndex(), (Double) getValue());
			break;
		case FLOAT:
			ps.setFloat(getIndex(), (Float) getValue());
			break;
		case INT:
			ps.setInt(getIndex(), (Integer) getValue());
			break;
		case OBJECT:
			ps.setObject(getIndex(), getValue());
			break;
		case SHORT:
			ps.setShort(getIndex(), (Short) getValue());
			break;
		default:
			break;
		}
	}
	
	public static class BindingTypeAdapter extends TypeAdapter<Binding> {
		@Override
		public Binding read(JsonReader reader) throws IOException {
			Integer index = null;
			Object value = null;
			SqlDataType type = null;
			JsonToken token = reader.peek();
			switch (token) {
			case BEGIN_ARRAY:
			case END_ARRAY:
				throw new IllegalArgumentException("arrays are not supported as a binding");
			case BEGIN_OBJECT:
				String name = reader.nextName();
				if (name.equalsIgnoreCase("index")) {
					index = reader.nextInt();
				} else if (name.equalsIgnoreCase("value")) {
					switch (reader.peek()) {
					case BEGIN_ARRAY:
					case END_ARRAY:
						throw new IllegalArgumentException("arrays are not supported as a value");
					case BEGIN_OBJECT:
						break;
					case BOOLEAN:
						value = reader.nextBoolean();
						break;
					case END_DOCUMENT:
						break;
					case END_OBJECT:
						break;
					case NAME:
						break;
					case NULL:
						break;
					case NUMBER:
						value = readNumber(reader);
						break;
					case STRING:
						value = reader.nextString();
						break;
					default:
						break;
					
					}
				} else if (name.equalsIgnoreCase("type")) {
					type = SqlDataType.lookup(reader.nextString());
				}
				return new Binding(index, value, type);
			case BOOLEAN:
				value = reader.nextBoolean();
				return new Binding(null, value, null);
			case END_DOCUMENT:
				break;
			case END_OBJECT:
				break;
			case NAME:
				break;
			case NULL:
				reader.nextNull();
				return null;
			case NUMBER:
				value = readNumber(reader);
				return new Binding(null, value, null);
			case STRING:
				value = reader.nextString();
				return new Binding(null, value, null);
			default:
				break;
			}
			return null;
		}
		
		private static Object readNumber(JsonReader reader) throws IOException {
			Number value = null;
			try {
				value = reader.nextInt();
			} catch (NumberFormatException e0) {
				try {
					value = reader.nextLong();
				} catch (NumberFormatException e1) {
					try {
						value = reader.nextDouble();
					} catch (NumberFormatException e2) {
						throw new RuntimeException(MessageFormat.format("Unable to read {0} as number", reader.nextString()));
					}
				}
			}
			return value;
		}

		@Override
		public void write(JsonWriter writer, Binding binding) throws IOException {
			if (binding == null) {
				writer.nullValue();
				return;
			}
			writer.beginObject();
			if (binding.getIndex() != null) {
				writer.name("index");
				writer.value(binding.getIndex());
			}
			if (binding.getTypeOfValue() != null) {
				writer.name("type");
				writer.value(binding.getTypeOfValue().name());
			}
			if (binding.getValue() != null) {
				writer.name("value");
				writer.value(GsonUtils.newGson().toJson(binding.getValue()));
			}
			writer.endObject();
		}
	}
	
	public static void bindAll(Binding[] bindings, PreparedStatement pstmt) throws SQLException {
		int lastIndex = 0;
		if (bindings != null && bindings.length > 0) {
			for (int i=0; i < bindings.length; ++i) {
				Binding b = bindings[i];
				if (b.getIndex() == null) {
					b.setIndex(++lastIndex);
				}
				bindings[i].bind(pstmt);
			}						
		}
	}

	public static class BindingsBuilder {
		private final List<Binding> bindings = new LinkedList<Binding>();
		
		public Binding[] build() {
			int lastIndex = 0;
			for (int i=0; i < this.bindings.size(); ++i) {
				Binding b = this.bindings.get(i);
				if (b.getIndex() == 0) {
					b.setIndex(++lastIndex);
				}
			}
			return this.bindings.toArray(new Binding[0]);
		}

		public BindingsBuilder add(final int index, final Object value, final String typeOfValue) {
			SqlDataType type = SqlDataType.lookup(typeOfValue);
			Binding b = new Binding(index, value, type);
			return this.add(b);
		}
		
		public BindingsBuilder add(final Binding binding) {
			// verify new binding will be compatible with existing bindings
			Iterator<Binding> iterator = this.bindings.iterator();
			while (iterator.hasNext()) {
				Binding b = iterator.next();
				if (b.getIndex() == binding.getIndex()) {
					iterator.remove();
				}
			}			
			this.bindings.add(binding);
			return this;
		}

		public BindingsBuilder clear() {
			this.bindings.clear();
			return this;
		}
	}
}

