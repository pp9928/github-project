using NUnit.Framework;

namespace OpenQA.Selenium.Firefox
{
    [TestFixture]
    public class FirefoxOptionsTest : DriverTestFixture
    {
        [Test]
        public void EnableHeadless()
        {
            var options = new FirefoxOptions()
            {
                EnableHeadless = true
            };

            Assert.IsTrue(options.EnableHeadless);
        }

        [Test]
        public void DisableHeadless()
        {
            var options = new FirefoxOptions();
            options.AddArgument("--headless");
            options.EnableHeadless = false;
            Assert.IsEmpty(options.Arguments);
        }
    }
}
