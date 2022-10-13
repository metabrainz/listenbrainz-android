package org.listenbrainz.android.presentation.features.adapters

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.data.sources.api.entities.EntityUtils.getDisplayArtist
import org.listenbrainz.android.data.sources.api.entities.mbentity.*
import java.lang.reflect.Type
import java.util.*

object ResultItemUtils {
    fun getEntityAsResultItem(entity: MBEntity?): ResultItem {
        val item: ResultItem? = when (entity) {
            is Artist -> {
                ResultItem(entity.mbid, entity.name, entity.disambiguation,
                    entity.type, entity.country)
            }
            is Event -> {
                when {
                    entity.lifeSpan != null -> {
                        ResultItem(entity.mbid, entity.name, entity.disambiguation, entity.type, entity.lifeSpan!!.timePeriod)
                    }
                    else -> {
                        ResultItem(entity.mbid, entity.name, entity.disambiguation, entity.type, "")
                    }
                }
            }
            is Instrument -> {
                ResultItem(entity.mbid, entity.name, entity.disambiguation, entity.description, entity.type)
            }
            is Label -> {
                ResultItem(entity.mbid, entity.name, entity.disambiguation, entity.type, entity.country)
            }
            is Recording -> {
                when {
                    entity.releases.size > 0 -> {
                        ResultItem(entity.mbid, entity.title, entity.disambiguation, entity.releases[0].title, getDisplayArtist(entity.artistCredits))
                    }
                    else -> {
                        ResultItem(entity.mbid, entity.title, entity.disambiguation, "", getDisplayArtist(entity.artistCredits))
                    }
                }
            }
            is Release -> {
                ResultItem(entity.mbid, entity.title, entity.disambiguation, getDisplayArtist(entity.artistCredits), entity.labelCatalog())
            }
            is ReleaseGroup -> {
                ResultItem(entity.mbid, entity.title, entity.disambiguation, getDisplayArtist(entity.getArtistCredits()), entity.fullType)
            }
            else -> null
        }
        return item!!
    }

    private fun getTypeToken(entity: MBEntityType): Type? {
        return when {
            entity === MBEntityType.ARTIST -> TypeToken.getParameterized(MutableList::class.java, Artist::class.java).type
            entity === MBEntityType.RELEASE -> TypeToken.getParameterized(MutableList::class.java, Release::class.java).type
            entity === MBEntityType.LABEL -> TypeToken.getParameterized(MutableList::class.java, Label::class.java).type
            entity === MBEntityType.RECORDING -> TypeToken.getParameterized(MutableList::class.java, Recording::class.java).type
            entity === MBEntityType.EVENT -> TypeToken.getParameterized(MutableList::class.java, Event::class.java).type
            entity === MBEntityType.INSTRUMENT -> TypeToken.getParameterized(MutableList::class.java, Instrument::class.java).type
            entity === MBEntityType.RELEASE_GROUP -> TypeToken.getParameterized(MutableList::class.java, ReleaseGroup::class.java).type
            else -> null
        }
    }

    fun getJSONResponseAsResultItemList(response: String?, entity: MBEntityType): List<ResultItem> {
        val list = Gson().fromJson<List<MBEntity>>(JsonParser.parseString(response).asJsonObject[entity.entity + "s"], getTypeToken(entity))
        val items: MutableList<ResultItem> = ArrayList()
        for (e in list) items.add(getEntityAsResultItem(e))
        return items
    }
}