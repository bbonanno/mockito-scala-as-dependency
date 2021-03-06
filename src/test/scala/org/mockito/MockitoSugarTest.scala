package org.mockito

import org.scalatest
import org.scalatest.WordSpec

class MockitoSugarTest extends WordSpec with MockitoSugar with scalatest.Matchers with ArgumentMatchersSugar with ByNameExperimental {

  class Foo {
    def bar = "not mocked"

    def iHaveSomeDefaultArguments(noDefault: String, default: String = "default value"): String =
      s"$noDefault - $default"

    def iHaveByNameArgs(normal: String, byName: => String, byName2: => String): String = s"$normal - $byName - $byName2"

    def iStartWithByNameArgs(byName: => String, normal: String): String = s"$normal - $byName"

    def iHaveFunction0Args(normal: String, f0: () => String): String = s"$normal - $f0"

    def iHaveByNameAndFunction0Args(normal: String, f0: () => String, byName: => String): String = s"$normal - $byName - $f0"
  }

  class Bar {
    def baz = "not mocked"

    def iAlsoHaveSomeDefaultArguments(
        noDefault: String,
        default: String = "default value"
    ): String = s"$noDefault - $default"
  }

  trait Baz {
    def traitMethod(defaultArg: Int = 30): Int = defaultArg + 12
  }

  class SomeClass extends Foo with Baz

  "mock[T]" should {
    "create a valid mock" in {
      val aMock = mock[Foo]

      when(aMock.bar) thenReturn "mocked!"

      aMock.bar shouldBe "mocked!"
    }

    "pre-configure the mock so it works with default arguments" in {
      val aMock = mock[Foo]

      aMock.iHaveSomeDefaultArguments("I'm not gonna pass the second argument")
      aMock.iHaveSomeDefaultArguments(
        "I'm gonna pass the second argument",
        "second argument"
      )

      verify(aMock).iHaveSomeDefaultArguments(
        "I'm not gonna pass the second argument",
        "default value"
      )
      verify(aMock).iHaveSomeDefaultArguments(
        "I'm gonna pass the second argument",
        "second argument"
      )
    }

    "create a mock with default answer" in {
      val aMock = mock[Foo](Answers.CALLS_REAL_METHODS)

      aMock.bar shouldBe "not mocked"
    }

    "create a mock with name" in {
      val aMock = mock[Foo]("Nice Mock")

      aMock.toString shouldBe "Nice Mock"
    }

    "work with inline mixins" in {
      val aMock = mock[Foo with Baz]

      when(aMock.bar) thenReturn "mocked!"
      when(aMock.traitMethod(any)) thenReturn 69

      aMock.bar shouldBe "mocked!"
      aMock.traitMethod() shouldBe 69

      verify(aMock).traitMethod(30)
    }

    "work with standard mixins" in {
      val aMock = mock[SomeClass]

      when(aMock.bar) thenReturn "mocked!"
      when(aMock.traitMethod(any)) thenReturn 69

      aMock.bar shouldBe "mocked!"
      aMock.traitMethod() shouldBe 69

      verify(aMock).traitMethod(30)
    }

    "work with by-name arguments (argument order doesn't matter when not using matchers)" in {
      val aMock = mock[Foo]

      when(aMock.iStartWithByNameArgs("arg1", "arg2")) thenReturn "mocked!"

      aMock.iStartWithByNameArgs("arg1", "arg2") shouldBe "mocked!"
      aMock.iStartWithByNameArgs("arg1", "arg3") shouldBe null

      verify(aMock).iStartWithByNameArgs("arg1", "arg2")
      verify(aMock).iStartWithByNameArgs("arg1", "arg3")
    }

    "work with by-name arguments and matchers (by-name arguments have to be the last ones when using matchers)" in {
      val aMock = mock[Foo]

      when(aMock.iHaveByNameArgs(any, any, any)) thenReturn "mocked!"

      aMock.iHaveByNameArgs("arg1", "arg2", "arg3") shouldBe "mocked!"

      verify(aMock).iHaveByNameArgs(eqTo("arg1"), endsWith("g2"), eqTo("arg3"))
    }

    "work with Function0 arguments" in {
      val aMock = mock[Foo]

      when(aMock.iHaveFunction0Args(eqTo("arg1"), function0("arg2"))) thenReturn "mocked!"

      aMock.iHaveFunction0Args("arg1", () => "arg2") shouldBe "mocked!"
      aMock.iHaveFunction0Args("arg1", () => "arg3") shouldBe null

      verify(aMock).iHaveFunction0Args(eqTo("arg1"), function0("arg2"))
      verify(aMock).iHaveFunction0Args(eqTo("arg1"), function0("arg3"))
    }

    "work with by-name and Function0 arguments (by-name arguments have to be the last ones when using matchers)" in {
      val aMock = mock[Foo]

      when(aMock.iHaveByNameAndFunction0Args(any, any, any)) thenReturn "mocked!"

      aMock.iHaveByNameAndFunction0Args("arg1", () => "arg2", "arg3") shouldBe "mocked!"

      verify(aMock).iHaveByNameAndFunction0Args(eqTo("arg1"), function0("arg2"), startsWith("arg"))
    }
  }

  "reset[T]" should {
    "reset the mock and re-configure the mock so it works with default arguments" in {
      val aMock       = mock[Foo]
      val anotherMock = mock[Bar]

      reset(aMock, anotherMock)

      aMock.iHaveSomeDefaultArguments("I'm not gonna pass the second argument")
      anotherMock.iAlsoHaveSomeDefaultArguments(
        "I'm not gonna pass the second argument"
      )

      verify(aMock).iHaveSomeDefaultArguments(
        "I'm not gonna pass the second argument",
        "default value"
      )
      verify(anotherMock).iAlsoHaveSomeDefaultArguments(
        "I'm not gonna pass the second argument",
        "default value"
      )
    }
  }

  "verifyNoMoreInteractions" should {
    "ignore the calls to the methods that provide default arguments" in {
      val aMock = mock[Foo]

      aMock.iHaveSomeDefaultArguments("I'm not gonna pass the second argument")

      verify(aMock).iHaveSomeDefaultArguments(
        "I'm not gonna pass the second argument",
        "default value"
      )
      verifyNoMoreInteractions(aMock)
    }
  }

  "argumentCaptor[T]" should {
    "deal with default arguments" in {
      val aMock = mock[Foo]

      aMock.iHaveSomeDefaultArguments("I'm not gonna pass the second argument")

      val captor1 = argumentCaptor[String]
      val captor2 = argumentCaptor[String]
      verify(aMock)
        .iHaveSomeDefaultArguments(captor1.capture(), captor2.capture())

      captor1.getValue shouldBe "I'm not gonna pass the second argument"
      captor2.getValue shouldBe "default value"
    }
  }

  "spy[T]" should {
    "create a valid spy" in {
      val aSpy = spy(new Foo)

      when(aSpy.bar) thenReturn "mocked!"

      aSpy.bar shouldBe "mocked!"
    }
  }

  "spyLambda[T]" should {
    "create a valid spy for lambdas and anonymous classes" in {
      val aSpy = spyLambda((arg: String) => s"Got: $arg")

      when(aSpy.apply(any)) thenReturn "mocked!"

      aSpy("hi!") shouldBe "mocked!"
      verify(aSpy).apply("hi!")
    }
  }
}
