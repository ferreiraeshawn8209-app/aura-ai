package com.aura.ai.data.local

import androidx.room.TypeConverter
import com.aura.ai.data.local.entity.MemoryCategory
import com.aura.ai.data.local.entity.MessageRole
import com.aura.ai.data.local.entity.SkillStatus
import com.aura.ai.data.local.entity.TaskOutcome

class Converters {
    @TypeConverter fun memoryCategory(value: String) = MemoryCategory.valueOf(value)
    @TypeConverter fun memoryCategory(value: MemoryCategory) = value.name

    @TypeConverter fun skillStatus(value: String) = SkillStatus.valueOf(value)
    @TypeConverter fun skillStatus(value: SkillStatus) = value.name

    @TypeConverter fun taskOutcome(value: String) = TaskOutcome.valueOf(value)
    @TypeConverter fun taskOutcome(value: TaskOutcome) = value.name

    @TypeConverter fun messageRole(value: String) = MessageRole.valueOf(value)
    @TypeConverter fun messageRole(value: MessageRole) = value.name
}
