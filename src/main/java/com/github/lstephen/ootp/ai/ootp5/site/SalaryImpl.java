package com.github.lstephen.ootp.ai.ootp5.site;

import com.github.lstephen.ootp.ai.data.Id;
import com.github.lstephen.ootp.ai.io.SalaryFormat;
import com.github.lstephen.ootp.ai.player.Player;
import com.github.lstephen.ootp.ai.player.PlayerId;
import com.github.lstephen.ootp.ai.roster.Team;
import com.github.lstephen.ootp.ai.site.Salary;
import com.github.lstephen.ootp.ai.site.Site;
import com.github.lstephen.ootp.extract.html.Page;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** @author lstephen */
public class SalaryImpl implements Salary {

  private static final Logger LOG = Logger.getLogger(SalaryImpl.class.getName());

  private final Id<Team> team;

  private final Site site;

  private static final Map<String, Integer> CURRENT_SALARY_CACHE = Maps.newHashMap();

  public SalaryImpl(Site site, Id<Team> team) {
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(team);

    this.site = site;
    this.team = team;
  }

  private Page getPage() {
    return site.getPage("team" + team.get() + "sa.html");
  }

  private Document loadPage() {
    return getPage().load();
  }

  public String getSalary(Player p) {
    return getSalary(p.getId());
  }

  public String getSalary(PlayerId id) {
    Document doc = loadPage();

    for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
      Element a = el.children().select("a").get(0);
      if (id.equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
        String salary = el.children().get(2).text().trim();

        if (salary.startsWith("$")) {
          String value;
          try {
            value =
                SalaryFormat.prettyPrint(
                    NumberFormat.getNumberInstance().parse(salary.substring(1)));
          } catch (ParseException e) {
            throw Throwables.propagate(e);
          }

          char years = el.children().get(4).text().trim().charAt(0);

          if (years != '1') {
            return value + "x" + years;
          } else {
            String comment = el.children().get(5).text();
            if (comment.contains("Extension")) {
              return value + " e";
            } else if (comment.contains("Automatic")) {
              return value + " r";
            } else if (comment.contains("Arbitration")) {
              return value + " a";
            } else {
              return value + "  ";
            }
          }
        } else {
          return "";
        }
      }
    }
    return null;
  }

  public Integer getCurrentSalary(final Player p) {
    return getCurrentSalary(p.getId());
  }

  public Integer getCurrentSalary(final PlayerId id) {
    String key = site.getName() + id;

    if (CURRENT_SALARY_CACHE.containsKey(key)) {
      return CURRENT_SALARY_CACHE.get(key);
    }

    Document doc = loadPage();

    for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
      Element a = el.children().select("a").get(0);
      if (id.equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
        String salary = el.children().get(2).text().trim();

        if (salary.startsWith("$")) {
          try {
            Integer result =
                NumberFormat.getIntegerInstance().parse(salary.substring(1)).intValue();

            CURRENT_SALARY_CACHE.put(key, result);

            return result;
          } catch (ParseException e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }

    return 0;
  }

  public Integer getNextSalary(Player p) {
    return getNextSalary(p.getId());
  }

  public Integer getNextSalary(PlayerId id) {
    Document doc = loadPage();

    for (Element el : doc.select("tr.g:has(a), tr.g2:has(a)")) {
      Element a = el.children().select("a").get(0);
      if (id.equals(new PlayerId(a.attr("href").replaceAll(".html", "")))) {
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

            LOG.log(Level.FINE, "Comment: " + comment);

            if (comment.contains("Automatic") || comment.contains("Possible Arbitration")) {
              return getCurrentSalary(id);
            } else if (comment.contains("Arbitration (Estimate:")) {
              LOG.log(Level.INFO, "Arbirtation: " + comment);
              try {
                int estimate =
                    NumberFormat.getIntegerInstance()
                        .parse(StringUtils.substringBetween(comment, "$", ")"))
                        .intValue();

                return Math.max(getCurrentSalary(id), estimate);
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
    return PlayerList.from(site, getPage()).extract();
  }
}
