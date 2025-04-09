package cz.fit.cvut.feature.language.data.db.mapper

import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel

internal class LanguageEntityMapper {
    fun mapLanguageEntityToModel(entity: TolgeeLanguageEntity): TolgeeLanguageModel {
        return TolgeeLanguageModel(
            id = entity.id,
            name = entity.name,
            originalName = entity.originalName,
            tag = entity.tag,
            flagEmoji = entity.flagEmoji,
            isBase = entity.isBase
        )
    }

    fun mapLanguageModelToEntity(model: TolgeeLanguageModel): TolgeeLanguageEntity {
        return TolgeeLanguageEntity(
            id = model.id,
            name = model.name,
            originalName = model.originalName,
            tag = model.tag,
            flagEmoji = model.flagEmoji,
            isBase = model.isBase
        )
    }
} 