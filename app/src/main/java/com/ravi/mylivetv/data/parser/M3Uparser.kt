package com.ravi.mylivetv.data.parser

import com.ravi.mylivetv.data.model.ChannelDto
import javax.inject.Inject

class M3UParser @Inject constructor() {

    fun parse(content: String): List<ChannelDto> {
        val lines = content.lines()
        val channels = mutableListOf<ChannelDto>()

        var name = ""
        var logo = ""
        var category = ""

        for (line in lines) {
            when {
                line.startsWith("#EXTINF") -> {
                    name = line.substringAfter(",").trim()
                    logo = Regex("""tvg-logo="(.*?)"""")
                        .find(line)?.groupValues?.get(1).orEmpty()
                    category = Regex("""group-title="(.*?)"""")
                        .find(line)?.groupValues?.get(1).orEmpty()
                }

                line.startsWith("http") -> {
                    channels.add(
                        ChannelDto(
                            name = name,
                            logo = logo,
                            streamUrl = line.trim(),
                            category = category
                        )
                    )
                }
            }
        }
        return channels
    }
}

