package com.ljs.scratch.ootp.ootp6.site

import com.ljs.ootp.ai.data.Id
import com.ljs.ootp.ai.player.Player
import com.ljs.ootp.ai.player.PlayerId
import com.ljs.ootp.ai.ootp5.site.SalaryImpl
import com.ljs.scratch.ootp.ootp5.site.MockSite
import org.apache.commons.lang3.RandomStringUtils._

import org.fest.assertions.api.Assertions._

import org.junit.runner.RunWith

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestSalary extends FlatSpec {

    "getSalary" should "find the salary for Victor Plata" in {
        val id = randomAlphanumeric(10)
        val site = new MockSite

        val salary = new SalaryImpl(site, Id.valueOf(id))

        site.expectGetPage(f"team$id%ssa.html")
        site.onLoadPage("/com/ljs/scratch/ootp/ootp6/site/pam_salary.html")

        val player = Player.create(new PlayerId("p241"), null, null)

        val result = salary.getSalary(player)

        assertThat(result).isEqualTo("$4.20m  ");
    }

}
