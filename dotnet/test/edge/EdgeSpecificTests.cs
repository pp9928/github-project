using NUnit.Framework;

namespace OpenQA.Selenium.Edge
{
    [TestFixture]
    public class EdgeSpecificTests : DriverTestFixture
    {
        [Test]
        public void EnableHeadless()
        {
            var options = new EdgeOptions()
            {
                EnableHeadless = true
            };

            Assert.IsTrue(options.EnableHeadless);
        }

        [Test]
        public void DisableOldHeadless()
        {
            var options = new EdgeOptions();
            options.AddArgument("--headless");
            options.EnableHeadless = false;
            Assert.IsEmpty(options.Arguments);
        }

        [Test]
        public void DisableNewHeadless()
        {
            var options = new EdgeOptions();
            options.AddArgument("--headless=chrome");
            options.EnableHeadless = false;
            Assert.IsEmpty(options.Arguments);
        }
    }
}
