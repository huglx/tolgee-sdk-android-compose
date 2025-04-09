package cz.fit.cvut.feature.translation.data.db.mapper

import cz.fit.cvut.feature.translation.data.db.entity.TolgeeKeyEntity
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity
import cz.fit.cvut.feature.translation.data.db.entity.TolgeeTranslationEntity
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel

internal class TolgeeEntityMapper {
    
    fun toEntity(domain: TolgeeKeyModel): Triple<TolgeeKeyEntity, List<TolgeeLanguageEntity>, List<TolgeeTranslationEntity>> {
        val keyEntity = TolgeeKeyEntity(
            keyId = domain.keyId,
            keyName = domain.keyName
        )
        
        val languageEntities = domain.translations.values.mapNotNull { translation ->
            translation.language?.let { language ->
                TolgeeLanguageEntity(
                    id = language.id,
                    name = language.name,
                    originalName = language.originalName,
                    tag = language.tag,
                    flagEmoji = language.flagEmoji,
                    isBase = language.isBase
                )
            }
        }.distinctBy { it.id }
        
        val translationEntities = domain.translations.values.mapNotNull { translation ->
            translation.language?.let { language ->
                TolgeeTranslationEntity(
                    keyId = domain.keyId,
                    languageId = language.id,
                    text = translation.text,
                    id = translation.id!!
                )
            }
        }
        
        return Triple(keyEntity, languageEntities, translationEntities)
    }
    
    fun toDomain(
        keyEntity: TolgeeKeyEntity,
        translationEntities: List<TolgeeTranslationEntity>,
        languageEntities: List<TolgeeLanguageEntity>
    ): TolgeeKeyModel {
        val languagesById = languageEntities.associateBy { it.id }
        
        return TolgeeKeyModel(
            keyId = keyEntity.keyId,
            keyName = keyEntity.keyName,
            translations = translationEntities.mapNotNull { entity ->
                languagesById[entity.languageId]?.let { languageEntity ->
                    languageEntity.tag to TolgeeTranslationModel(
                        text = entity.text,
                        id = entity.id,
                        language = TolgeeLanguageModel(
                            id = languageEntity.id,
                            name = languageEntity.name,
                            originalName = languageEntity.originalName,
                            tag = languageEntity.tag,
                            flagEmoji = languageEntity.flagEmoji,
                            isBase = languageEntity.isBase
                        )
                    )
                }
            }.toMap()
        )
    }

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