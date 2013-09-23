package com.ljs.scratch.ootp.html;

import com.google.common.base.Throwables;
import com.ljs.scratch.ootp.core.Player;
import com.ljs.scratch.ootp.core.PlayerId;
import com.ljs.scratch.ootp.core.TeamId;
import com.ljs.scratch.ootp.html.page.Page;
import java.text.NumberFormat;
import java.text.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author lstephen
 */
public class Salary {

    private Site site;

    private Page page;

    public Salary(Site site, TeamId team) {
        this.site = site;
        page = site.getPage("team" + team.unwrap() + "sa.html");
    }

    public String getSalary(Player p) {
        Document doc = page.load();

        for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
            Element a = el.children().select("a").get(0);
            if (p.getId().equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
                String salary = el.children().get(2).text().trim();

                if (salary.startsWith("$")) {
                    char years = el.children().get(4).text().trim().charAt(0);

                    if (years != '1') {
                        return salary + "x" + years;
                    } else {
                        String comment = el.children().get(5).text();
                        if (comment.contains("Extension")) {
                            return salary + " e";
                        } else if (comment.contains("Automatic")) {
                            return salary + " r";
                        } else if (comment.contains("Arbitration")) {
                            return salary + " a";
                        } else {
                            return salary + "  ";
                        }
                    }
                } else {
                    return "";
                }
            }
        }
        return null;
    }

    public Integer getCurrentSalary(Player p) {
        Document doc = page.load();

        for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
            Element a = el.children().select("a").get(0);
            if (p.getId().equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
                String salary = el.children().get(2).text().trim();

                if (salary.startsWith("$")) {
                    try {
                        return NumberFormat.getIntegerInstance().parse(salary.substring(1)).intValue();
                    } catch (ParseException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
        }

        return 0;
    }

    public Integer getNextSalary(Player p) {
        Document doc = page.load();

        for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
            Element a = el.children().select("a").get(0);
            if (p.getId().equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
                String salary = el.children().get(2).text().trim();

                if (salary.startsWith("$")) {
                    char years = el.children().get(4).text().trim().charAt(0);
                    if (years != '1') {
                        try {
                            return NumberFormat.getIntegerInstance().parse(salary.substring(1)).intValue();
                        } catch (ParseException e) {
                            throw Throwables.propagate(e);
                        }
                    } else {
                        String comment = el.children().get(5).text().trim();

                        if (comment.contains("Automatic") || comment.contains("Possible Arbitration")) {
                            return getCurrentSalary(p);
                        } else if (comment.contains("Arbitration (Estimate:")) {
                            try {
                                int estimate = NumberFormat.getIntegerInstance().parse(
                                    StringUtils.substringBetween(comment, "$", ")")).intValue();

                                return Math.max(getCurrentSalary(p), estimate);
                            } catch (ParseException e) {
                                throw Throwables.propagate(e);
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    public Iterable<Player> getSalariedPlayers() {
        return PlayerList.from(site, page).extract();
    }
}
