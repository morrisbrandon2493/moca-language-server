package com.github.mrglassdanny.mocalanguageserver.moca.lang.mocasql.format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.mrglassdanny.mocalanguageserver.MocaLanguageServer;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlLexer;
import com.github.mrglassdanny.mocalanguageserver.moca.lang.antlr.MocaSqlParser;

public class MocaSqlFormatter {

        private static String FORMATTING_TRAINING_DIR_FRAGMENT = "\\formatting\\training\\mocasql\\";

        private static org.antlr.codebuff.misc.LangDescriptor mocaSqlLangDescriptor = null;
        private static org.antlr.codebuff.Corpus mocaSqlCorpus = null;

        public static void configureAndTrain(String corpusDirName) throws Exception {
                mocaSqlLangDescriptor = new org.antlr.codebuff.misc.LangDescriptor("MocaSql",
                                MocaLanguageServer.globalStoragePath + FORMATTING_TRAINING_DIR_FRAGMENT + corpusDirName,
                                ".*\\.mocasql", MocaSqlLexer.class, MocaSqlParser.class, "moca_sql_script", 4,
                                MocaSqlLexer.LINE_COMMENT);

                mocaSqlCorpus = org.antlr.codebuff.Tool.trainCorpusForMocaLanguageServer(mocaSqlLangDescriptor);
        }

        public static String format(String src) {

                String dst = null;
                try {
                        dst = org.antlr.codebuff.Tool.formatForMocaLanguageServer(
                                        MocaSqlFormatter.mocaSqlLangDescriptor, src, MocaSqlFormatter.mocaSqlCorpus);
                } catch (Exception e) {
                }

                return dst;
        }

        public static void createDefaults() throws IOException {

                // Main default:
                {
                        final String defaultPath = MocaLanguageServer.globalStoragePath
                                        + FORMATTING_TRAINING_DIR_FRAGMENT + "\\default";

                        Files.createDirectories(Paths.get(defaultPath));

                        String example1 = "select a, \n" + "       b, \n" + "       c, \n" + "       d, \n"
                                        + "       e \n" + "from   l \n" + "where  l.a = '' \n"
                                        + "       and l.b = 'asdf' \n" + "       and c = 'asdf' \n"
                                        + "       and d = 10\n" + "\t   \n" + "select distinct a, \n"
                                        + "                b, \n" + "                c, \n" + "                d, \n"
                                        + "                e \n" + "from   l \n" + "       join z \n"
                                        + "         on l.a = z.a \n" + "            and l.b = z.b \n"
                                        + "       left join oo \n" + "              on z.a = o.a \n"
                                        + "       right join zz \n" + "               on z.z = zz.z \n"
                                        + "where  l.a = '' \n" + "       and l.b = 'asdf' \n"
                                        + "       and c = 'asdf' \n" + "       and d = 10 \n" + "group  by a, \n"
                                        + "          b, \n" + "          c \n" + "order  by a, \n" + "          b, \n"
                                        + "          c \n" + "\n" + "insert into l \n" + "            (a, \n"
                                        + "             b, \n" + "             c, \n" + "             d) \n"
                                        + "values      ('asdf', \n" + "             asdf, \n"
                                        + "             'asdf', \n" + "             654) \n" + "\t\t\t \n"
                                        + "update abc \n" + "set    a = '', \n" + "       b = 10, \n"
                                        + "       c = asdf \n" + "where  a = '' \n" + "       and abc.b = 10 \n"
                                        + "       and abc.c = asdf \n" + "\t   \n" + "delete from abc \n"
                                        + "where  a = 10 \n" + "       and b = 'asdf' \n" + "       and abc.d = foo \n"
                                        + "       and abc.e = bar \n" + "\t   \n" + "select a, \n" + "       b, \n"
                                        + "       c, \n" + "       d, \n" + "       e \n" + "from   l \n"
                                        + "where  l.a = '' \n" + "       and l.b = 'asdf' \n"
                                        + "       and c = 'asdf' \n" + "       and d = 10\n" + "\t   \n"
                                        + "select distinct a, \n" + "                b, \n" + "                c, \n"
                                        + "                d, \n" + "                e \n" + "from   l \n"
                                        + "       join z \n" + "         on l.a = z.a \n"
                                        + "            and l.b = z.b \n" + "       left join oo \n"
                                        + "              on z.a = o.a \n" + "       right join zz \n"
                                        + "               on z.z = zz.z \n" + "where  l.a = '' \n"
                                        + "       and l.b = @asdlkfj:raw\n" + "       and @+asdf\n" + "       and @* \n"
                                        + "group  by a, \n" + "          b, \n" + "          c \n" + "order  by a, \n"
                                        + "          b, \n" + "          c \n" + "\n" + "insert into l \n"
                                        + "            (a, \n" + "             b, \n" + "             c, \n"
                                        + "             d) \n" + "values      ('asdf', \n" + "             asdf, \n"
                                        + "             'asdf', \n" + "             654) \n" + "\t\t\t \n"
                                        + "update abc \n" + "set    a = '', \n" + "       b = 10, \n"
                                        + "       c = asdf \n" + "where  a = '' \n" + "       and abc.b = 10 \n"
                                        + "       and abc.c = nvl(asdf, @asdlfkj) \n" + "\t   \n" + "delete from abc \n"
                                        + "where  a = 10 \n" + "       and b = 'asdf' \n"
                                        + "       and abc.d = @@foo \n" + "       and abc.e = @bar ";

                        Files.write(Paths.get(defaultPath + "\\example1.mocasql"), example1.getBytes());
                }

        }

}