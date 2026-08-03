package Framework.LazberryRegistryFramework;

import Framework.LazberryRegistryFramework.Annotation.Column;
import Framework.LazberryRegistryFramework.Annotation.Id;
import Framework.LazberryRegistryFramework.Annotation.Table;
import Framework.LazberryRegistryFramework.FrameworkExceptions.IdMissingColumnException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Getter
public class EntityMetadata {
	private final Class<?> entityClass;
	private final String tableName;
	private Field idField;
	private final Map<String, Field> columnMap = new HashMap<>();

	public EntityMetadata(@NotNull Class<?> entityClass) {
		this.entityClass = entityClass;
		Table tableAnn = entityClass.getAnnotation(Table.class);
		this.tableName = (tableAnn.value().isEmpty()) ? entityClass.getSimpleName().toLowerCase() : tableAnn.value();

		try {
			entityClass.getDeclaredConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("[LRF-ORM] Entity " + entityClass.getSimpleName() + " MUST have a public/protected no-args constructor!");
		}

		for (Field field : entityClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				field.setAccessible(true);
				this.idField = field;

				String colName = getColumnName(field);
				columnMap.put(colName, field);
			}
			else if (field.isAnnotationPresent(Column.class)) {
				field.setAccessible(true);
				String colName = getColumnName(field);
				columnMap.put(colName, field);
			}
		}

		if (this.idField == null) {
			throw new IdMissingColumnException("[LRF-ORM] Entity " + entityClass.getSimpleName() + " MUST have at least one @Id field!");
		}
	}

	private String getColumnName(Field field) {
		if (field.isAnnotationPresent(Column.class)) {
			String name = field.getAnnotation(Column.class).name();
			if (!name.isEmpty()) return name;
		}
		return field.getName();
	}
}
