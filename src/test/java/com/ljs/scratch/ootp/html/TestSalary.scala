package com.ljs.scratch.ootp.html

import com.ljs.scratch.ootp.player.Player
import com.ljs.scratch.ootp.player.PlayerId
import com.ljs.scratch.ootp.roster.TeamId
import com.ljs.scratch.ootp.site.SiteDefinition

import org.junit._
import org.junit.runner.RunWith
import Assert._

import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.rule.PowerMockRule
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

import org.apache.commons.lang3.RandomStringUtils._

import org.powermock.api.easymock.PowerMock._

@RunWith(classOf[JUnitRunner])
@PrepareForTest(Array(classOf[Player]))
class TestSalary extends FlatSpec {

    @Rule
    val powerMock = new PowerMockRule()

    "getSalary" should "find the salary for Victor Plata" in {
        val id = randomAlphanumeric(10)
        val site = new MockSite

        val salary = new Salary(site, new TeamId(id))

        site.expectGetPage(f"team$id%ssa.html")
        site.onLoadPage("/com/ljs/scratch/ootp/html/pam_salary_ootp6.html")

        val player = Player.create(new PlayerId("p241"), null, null)


        replayAll()

        val result = salary.getSalary(player)

        verifyAll

        assert(result == "$4,200,000  ")
    }

}
