package com.github.lstephen.ootp.ai.roster;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.github.lstephen.ootp.ai.config.Config;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.site.Site;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author lstephen
 */
public final class Changes {

    public static enum ChangeType {
        ACQUISITION('a'), RELEASE('r'), FORCE_ML('m'), FORCE_FORCE_ML('M'), DONT_ACQUIRE('d'), PICKED('p'), FOURTY_MAN('f');

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

    public ImmutableSet<Player> get(ChangeType type) {
      return ImmutableSet.copyOf(changes.get(type));
    }

    private void add(ChangeType type, Player p) {
        changes.put(type, p);
    }

    private void add(ChangeType type, String name, Iterable<Player> ps) {
        for (Player p : ps) {
            if (p.getShortName().equals(name.trim())) {
                add(type, p);
            }
        }
    }

    private static String getChangesDir() throws IOException {
        Config config = Config.createDefault();

        return config
            .getValue("changes.dir")
            .or(config.getValue("output.dir").or("c:/ootp"));
    }

    public static Changes empty() {
      return new Changes();
    }

    public static Changes load(Site site) {
        Changes changes = Changes.empty();

        try {
            File src = new File(getChangesDir(), site.getName() + ".changes.txt");

            if (!src.exists()) {
                return changes;
            }

            Iterable<Player> draftList = null;
            Iterable<Player> faList = null;
            Iterable<Player> team = null;

            for (String line : Files.readLines(src, Charsets.UTF_8)) {
                if (Strings.isNullOrEmpty(line)) {
                    continue;
                }

                if (line.charAt(0) == '#') {
                    continue;
                }

                ChangeType type = ChangeType.valueOf(line.charAt(0));

                String raw = StringUtils.substringAfter(line, ",");

                if (raw.charAt(0) == 'p') {
                    PlayerId id = new PlayerId(raw);

                    changes.add(type, site.getPlayer(id));
                } else {
                    switch (type) {
                        case PICKED:
                            if (draftList == null) {
                                draftList = site.getDraft();
                            }
                            changes.add(type, raw, draftList);
                            break;
                        case DONT_ACQUIRE:
                            if (faList == null) {
                                faList = site.getFreeAgents();
                            }
                            changes.add(type, raw, faList);
                            break;
                        case RELEASE:
                        case FORCE_ML:
                        case FORCE_FORCE_ML:
                        case FOURTY_MAN:
                            if (team == null) {
                                team = site.getSingleTeam().getRoster().getAllPlayers();
                            }
                            changes.add(type, raw, team);
                            break;

                        default:
                            // do nothing
                    }
                }
            }

            return changes;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

    }

}
