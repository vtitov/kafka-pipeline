package com.github.vtitov.kafka.pipeline

package object figlet {
    import com.github.lalyos.jfiglet.FigletFont
    //lazy val figletFontName = "flf/standard.flf"
    lazy val figletFontName = "flf/smslant.flf"
    private lazy val figletFont = new FigletFont(classOf[FigletFont].getClassLoader.getResourceAsStream(figletFontName))
    def duplicateOneLine(s:String):String = s"$s\n${convertOneLine(s)}"
    def convertOneLine(s:String):String = figletFont.convert(s)
}
