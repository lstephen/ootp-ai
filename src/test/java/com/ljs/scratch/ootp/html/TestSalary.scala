package com.ljs.scratch.ootp.html

import com.ljs.scratch.ootp.player.Player
import com.ljs.scratch.ootp.player.PlayerId
import com.ljs.scratch.ootp.roster.TeamId

import org.apache.commons.lang3.RandomStringUtils._

import org.junit._
import org.junit.runner.RunWith

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestSalary extends FlatSpec {

    "getSalary" should "find the salary for Victor Plata" in {
        val id = randomAlphanumeric(10)
        val site = new MockSite

        val salary = new Salary(site, new TeamId(id))

        site.expectGetPage(f"team$id%ssa.html")
        site.onLoadPage("/com/ljs/scratch/ootp/html/pam_salary_ootp6.html")

        val player = Player.create(new PlayerId("p241"), null, null)

        val result = salary.getSalary(player)

        assert(result == "$4,200,000  ")
    }

}
