package com.aura.ai.data.local

import androidx.room.TypeConverter
import com.aura.ai.data.local.entity.MessageRole
import com.aura.ai.data.local.entity.TaskOutcome

class Converters {
    @TypeConverter     @TypeConverter fun taskOutcome(value: String) = TaskOutcome.valueOf(value)
    @TypeConverter fun taskOutcome(value: TaskOutcome) = value.name

    @TypeConverter fun messageRole(value: String) = MessageRole.valueOf(value)
    @TypeConverter fun messageRole(value: MessageRole) = value.name
}
