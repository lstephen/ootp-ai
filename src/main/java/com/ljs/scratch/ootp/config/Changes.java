package com.ljs.scratch.ootp.config;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.html.PlayerList;
import com.ljs.scratch.ootp.html.Site;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public final class Changes {

    public static enum ChangeType {
        ACQUISITION('a'), RELEASE('r'), FORCE_ML('m'), DONT_ACQUIRE('d'), PICKED('p');

        private final Character code;

        ChangeType(char code) {
            this.code = code;
        }

        public static ChangeType valueOf(Character code) {
            for (ChangeType ct : ChangeType.values()) {
                if (ct.code.equals(code)) {
                    return ct;
                }
            }

            throw new IllegalStateException();
        }

    }

    private final Multimap<ChangeType, Player> changes = HashMultimap.create();

    private Changes() { }

    public Iterable<Player> get(ChangeType type) {
        return changes.get(type);
    }

    private void add(ChangeType type, Player p) {
        changes.put(type, p);
    }

    public static Changes load(Site site) {
        Changes changes = new Changes();

        File src = new File(Directories.OUT, site.getName() + ".changes.txt");

        if (!src.exists()) {
            return changes;
        }

        Iterable<Player> draftList = null;

        try {
            for (String line : Files.readLines(src, Charsets.UTF_8)) {
                if (Strings.isNullOrEmpty(line)) {
                    continue;
                }

                if (line.charAt(0) == '#') {
                    continue;
                }

                ChangeType type = ChangeType.valueOf(line.charAt(0));

                String raw = StringUtils.substringAfter(line, ",");

                if (type == ChangeType.PICKED) {
                    if (draftList == null) {
                        draftList = PlayerList.draft(site).extract();
                    }
                    for (Player p : draftList) {
                        if (p.getShortName().equals(raw)) {
                            changes.add(type, p);
                        }
                    }
                } else {
                    PlayerId id = new PlayerId(raw);

                    changes.add(type, site.getPlayer(id).extract());
                }
            }

            return changes;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

    }

}
