using System;
using NUnit.Framework;
using OpenQA.Selenium.Environment;

namespace OpenQA.Selenium.Chrome
{
    [TestFixture]
    public class ChromeSpecificTests : DriverTestFixture
    {
        [OneTimeTearDown]
        public void RunAfterAnyTests()
        {
            EnvironmentManager.Instance.CloseCurrentDriver();
            EnvironmentManager.Instance.WebServer.Stop();
        }

        [Test]
        public void W3CFalse()
        {
            Assert.Throws<ArgumentException>(() => new ChromeOptions
            {
                UseSpecCompliantProtocol = false
            });
        }

        [Test]
        public void EnableHeadless()
        {
            var options = new ChromeOptions
            {
                EnableHeadless = true
            };

            Assert.IsTrue(options.EnableHeadless);
        }

        [Test]
        public void DisableOldHeadless()
        {
            var options = new ChromeOptions();
            options.AddArgument("--headless");
            options.EnableHeadless = false;
            Assert.IsEmpty(options.Arguments);
        }

        [Test]
        public void DisableNewHeadless()
        {
            var options = new ChromeOptions();
            options.AddArgument("--headless=chrome");
            options.EnableHeadless = false;
            Assert.IsEmpty(options.Arguments);
        }
    }
}
