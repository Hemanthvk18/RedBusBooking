package BusBooking;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.ElementClickInterceptedException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;

public class WeekendBusBookingMain {

	public static void main(String[] args) throws InterruptedException  {

		ChromeOptions option = new ChromeOptions();

		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		
		JavascriptExecutor js =(JavascriptExecutor) driver;
		Actions actions = new Actions(driver);

		/*--------------------------------- Select the to and from locations ---------------------------------*/
		driver.get("https://www.redbus.in/");
		String from="Bangaluru";
		String to="Mumbai";
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'From')]")));
		driver.findElement(By.xpath("//div[contains(text(),'From')]")).click();
		Thread.sleep(2000);
		driver.switchTo().activeElement().sendKeys(from);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'listItem')]"))); 
		driver.findElement(By.xpath("//div[contains(@class,'listItem')]")).click();
		driver.switchTo().activeElement().sendKeys(to);
		driver.findElement(By.xpath("//div[contains(@class,'listItem')]//div[contains(text(),'Mumbai')]")).click();

		/*--------------------------------- Click on Current date and search the buses---------------------------------*/
		driver.findElement(By.xpath("//div[contains(@class,'dateInputWrapper')]")).click();
		driver.switchTo().activeElement(); // switched to calendar window
		
		//Current date
		WebElement currentDayWebElement = driver
				.findElement(By.xpath("//div[contains(@data-datetype,'SELECTED')]//span"));
//		currentDayWebElement.click();
		
		//Click on upcoming Saturday in calendar
		String currentDay = currentDayWebElement.getText();
		int daysCount = daysToCurrentSat(Integer.parseInt(currentDay));
		//System.out.println("days to next Sunday: " + daysCount);
		if (daysCount > 0) {
			driver.findElement(
					By.xpath("(//div[contains(@data-datetype,'SELECTED')]//following::li)[" + daysCount + "]")).click();
		}
		

		driver.findElement(By.xpath("//button[contains(text(),'Search buses')]")).click();

		/*--------------------------------- Apply the suitable filters ---------------------------------*/
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Bus features')]")));
		WebElement Bus_features = driver.findElement(By.xpath("//div[contains(text(),'Bus features')]"));
		js.executeScript("arguments[0].scrollIntoView(true);", Bus_features);
		Bus_features.click();
		//driver.findElement(By.xpath("//div[contains(text(),'Primo Bus')]")).click();
		driver.findElement(By.xpath("(//div[contains(text(),'Live Tracking')])[2]")).click();
		Bus_features.click();

		WebElement Departure_time_from_source = driver
				.findElement(By.xpath("//div[contains(text(),'Departure time from source') and contains(@class,'title')]"));
		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(Departure_time_from_source));
			
			Departure_time_from_source.click();
		}
		catch(ElementClickInterceptedException e)
		{
			WebElement Departure_time_from_source1 = driver
					.findElement(By.xpath("//div[contains(text(),'Departure time from source') and contains(@class,'title')]"));
			js.executeScript("arguments[0].scrollIntoView(true);", Departure_time_from_source1);
			js.executeScript("arguments[0].click();", Departure_time_from_source1);
		}

		/*--------------------------------- Check the buses are available in shift and click ---------------------------------*/	
		System.out.println("Booking Details from "+from+" to "+to);
		boolean slotSelected = false;
		// Morning Slot
		if (!slotSelected && driver.findElements(By.xpath("//div[contains(text(),'Morning')]")).size() > 0) {
		    WebElement morning = driver.findElement(By.xpath("//div[contains(text(),'Morning')]"));
		    if (morning.isDisplayed() && morning.isEnabled()) {
		        morning.click();
		        System.out.println("Slot :- Buses are available in Morning");
		        slotSelected = true;
		    }
		}

		// Afternoon Slot
		if (!slotSelected && driver.findElements(By.xpath("//div[contains(text(),'Afternoon')]")).size() > 0) {
		    WebElement afternoon = driver.findElement(By.xpath("//div[contains(text(),'Afternoon')]"));
		    if (afternoon.isDisplayed() && afternoon.isEnabled()) {
		        afternoon.click();
		        System.out.println("Slot :- Buses are available in Afternoon");
		        slotSelected = true;
		    }
		}

		// Evening Slot
		if (!slotSelected && driver.findElements(By.xpath("//div[contains(text(),'Evening')]")).size() > 0) {
		    WebElement evening = driver.findElement(By.xpath("//div[contains(text(),'Evening')]"));
		    if (evening.isDisplayed() && evening.isEnabled()) {
		        evening.click();
		        System.out.println("Slot :- Buses are available in Evening");
		        slotSelected = true;
		    }
		}
		
		// Night Slot
				if (!slotSelected && driver.findElements(By.xpath("//div[contains(text(),'Night')]")).size() > 0) {
				    WebElement evening = driver.findElement(By.xpath("//div[contains(text(),'Night')]"));
				    if (evening.isDisplayed() && evening.isEnabled()) {
				        evening.click();
				        System.out.println("Slot :- Buses are available in Night");
				        slotSelected = true;
				    }
				}

		// Final Check
		if (!slotSelected) {
		    System.out.println("Slot :- No slots available");
		}


		
		Departure_time_from_source.click();

		/*--------------------------------- Check the buses count after applying filters ---------------------------------*/

		List<WebElement> buses;
		String totalBusesText = driver
				.findElement(By.xpath("//div[@class='busesFoundText__ind-search-styles-module-scss-PHVGD']")).getText();
		System.out.println("Total Buses avialable :- " + totalBusesText);

		/*--------------------------------- Get the least price in the available buses ---------------------------------*/
		int busCount = Integer.parseInt(totalBusesText.split(" ")[0]);
		Set<WebElement> totalBuses = new HashSet<>();
		Integer leastPrice = Integer.MAX_VALUE;
		WebElement leastBusPrice = null;

		for (int i = 1; i <= busCount; i++) {

			WebElement busDetails = driver.findElement(By.xpath("(//li[contains(@class,'tupleWrapper')])[" + i + "]"));
			wait.until(ExpectedConditions.visibilityOf(busDetails)); 
			js.executeScript("arguments[0].scrollIntoView(true);", busDetails);

			totalBuses.add(busDetails);
			wait.until(ExpectedConditions.visibilityOf(busDetails.findElement(By.xpath(".//p[contains(@class,'finalFare')]"))));
			String curr_price_String = busDetails.findElement(By.xpath(".//p[contains(@class,'finalFare')]")).getText();

			StringBuilder sb = new StringBuilder();

			for (int j = 0; j < curr_price_String.length(); j++) {
				char ch = curr_price_String.charAt(j);
				if (Character.isDigit(ch)) {
					sb.append(ch);
				}
			}

			String realsbString = sb.toString();

			int realPrice = Integer.parseInt(realsbString);

			if (realPrice < leastPrice) {
				leastPrice = realPrice;
				leastBusPrice = busDetails;
			}

		}

		System.out.println("least Price is:- "
				+ leastBusPrice.findElement(By.xpath(".//p[contains(@class,'finalFare')]")).getText());

		/*--------------------------------- Navigate to that least price bus and click on it ---------------------------------*/
		js.executeScript("arguments[0].scrollIntoView(true);", leastBusPrice);
		leastBusPrice.findElement(By.xpath(".//button[contains(text(),'View seats')]")).click();

		/*--------------------------------- Close pop up  ---------------------------------*/ // D

		try {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-autoid='bottom-sheet']")));
		List<WebElement> popupCheck = driver.findElements(By.xpath("//div[@data-autoid='bottom-sheet']"));
		
		if (popupCheck.size() > 0) {
		    //System.out.println("Popup appeared. Handling it...");
		    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'header')]/div/button/i")));
		    WebElement phoneNumberPopup = driver.findElement(By.xpath("//div[contains(@class,'header')]/div/button/i"));
		    js.executeScript("arguments[0].scrollIntoView(true);", phoneNumberPopup);
	    	js.executeScript("arguments[0].click();", phoneNumberPopup);
		} else {
		    //System.out.println("Popup did not appear. Continuing test...");
		}
		}
		catch(Exception e)
		{
			
		}

	

		/*--------------------------------- Select lowest Seat available ---------------------------------*/

		List<WebElement> checkSlodStatus = driver
				.findElements(By.xpath("//span[contains(@class,'sleeper__ind-seat-styles-module-scss') and @role='button']/span[1]"));
		int minPrice = Integer.MAX_VALUE; // Start with the highest possible value
		WebElement lowestSeat = null;
		boolean seatAvailability = false;
		System.out.println("Total seat capacity of bus :- " + checkSlodStatus.size());
	
		
		for (WebElement lowPriceElement : checkSlodStatus) {
			if (!lowPriceElement.getText().equalsIgnoreCase("sold") && !lowPriceElement.getText().isEmpty()) {
				seatAvailability = true;
				String numaricValue = lowPriceElement.getText().trim().replaceAll("[^0-9]", "");
				// String numaricValue=lowPriceElement.getText().trim().split("₹")[1];
				int price = Integer.parseInt(numaricValue);
				if (price < minPrice) {
					minPrice = price;
					lowestSeat = lowPriceElement;

				}
			}

		}
		if (!seatAvailability) {
			System.out.println("All seats are boooking in this bus....!!!!");
		}

		if (lowestSeat != null) {
			System.out.println("Lowest seat in the bus:- ₹" + minPrice);
			//span[contains(@class,'sleeper__ind-seat-styles-module-scss') and @role='button']/span[1]/parent::span 
			lowestSeat.findElement(By.xpath("./parent::span")).click();
		}

		/*--------------------------------- Select board & dropping point ---------------------------------*/

		driver.findElement(By.xpath("//button[contains(text(),'Select boarding & dropping points')]")).click();
		driver.findElement(By.xpath("//div[@aria-label='Boarding points']/div[contains(@class,'bpdpSelection')]")).click();
		driver.findElement(By.xpath("//div[@aria-label='Dropping points']/div[contains(@class,'bpdpSelection')]")).click();

		/*--------------------------------- Enter the passenger info ---------------------------------*/
		WebElement ContactNum=driver.findElement(By.xpath("//input[@placeholder='Phone']"));
		wait.until(ExpectedConditions.elementToBeClickable(ContactNum));
		ContactNum.sendKeys("7977464893");
		driver.findElement(By.xpath("//input[@placeholder='Enter email id']")).sendKeys("Demo@gmail.com");
		

		driver.findElement(By.id("0_201")).click(); // selecting state of resistance, here pop up opens
		WebElement searchState= driver.findElement(By.xpath("//input[contains(@class,'searchInput') and @placeholder='Search for state']"));
		wait.until(ExpectedConditions.elementToBeClickable(searchState));
		searchState.sendKeys("Karna");
		WebElement ExpetedState=driver.findElement(By.xpath("//div[contains(@class,'listItem')]//div[contains(text(),'Karnataka')]"));
		wait.until(ExpectedConditions.elementToBeClickable(ExpetedState));
		ExpetedState.click();
		
		js.executeScript("window.scrollBy(0, 200);");
		driver.findElement(By.xpath("//input[@placeholder='Enter your Name']")).sendKeys("Hemanth");
		driver.findElement(By.xpath("//input[@placeholder='Enter Age']")).sendKeys("26");
		
		List<WebElement> gender=driver.findElements(By.xpath("//label[text()='Male']"));
		if(!gender.isEmpty())
		{
			WebElement genderElement = gender.get(0);
			if(genderElement.isEnabled())
			{
				genderElement.click();
			}
		}
		else
		{
			System.out.println("Seat Type :- You are selected reserved seat");
		}

		/*--------------------------------- Bus cancellation fee and Insurance ---------------------------------*/
		
		//check the cancellation is available for this bus
		List<WebElement> rejections = driver.findElements(By.id("fcRejectText"));
		if (!rejections.isEmpty()) {
		    WebElement rejectionElement = rejections.get(0);
		    if (rejectionElement.isEnabled()) {
		        js.executeScript("arguments[0].scrollIntoView(true);", rejectionElement);
		        rejectionElement.click();
		    }
		} else {
		    System.out.println("Cancellation status :- Cancellation is not available for this Bus..!!!");
		}

		WebElement Insurance = driver.findElement(By.id("insuranceConfirmText"));
		js.executeScript("arguments[0].scrollIntoView(true);", Insurance);
		Insurance.click(); // opting insurance
		
		WebElement ContinueBookingButton= driver.findElement(By.xpath("//button[contains(text(),'Continue booking')]"));
		js.executeScript("arguments[0].scrollIntoView(true);", ContinueBookingButton);
		ContinueBookingButton.click();		

		/*--------------------------------- Select the payment mode ---------------------------------*/
		WebElement finaleAmount=driver.findElement(By.xpath("//div[@class='fare']"));
		wait.until(ExpectedConditions.elementToBeClickable(finaleAmount));
		js.executeScript("arguments[0].scrollIntoView(true);", finaleAmount);
		System.out.println("Total amount after GST :- "+finaleAmount.getText());
		
		actions.sendKeys(Keys.HOME).perform();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-autoid='journeyDetailsExpanded']//div[contains(@class,'travelsName__')]")));
		String BusName=driver.findElement(By.xpath("//div[@data-autoid='journeyDetailsExpanded']//div[contains(@class,'travelsName__')]")).getText();
		String SeatDetalis=driver.findElement(By.xpath("//div[@data-autoid='journeyDetailsExpanded']//div[contains(@class,'travelsType___5401eb')]")).getText();
		System.out.println("Bus Info :- "+BusName+" -- "+SeatDetalis);
		
		WebElement PaymentMode= wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Pay through UPI ID')]")));
		
		try {
			PaymentMode.click();
		} catch (ElementClickInterceptedException e) {
			System.err.println("Click intercepted at Payment page");
		}
		
		
		WebElement UPI=driver.findElement(By.xpath("//input[contains(@class,'inputField')]"));
		UPI.sendKeys("vk@okaxis");
		String expetedText="Invalid UPI Id. Please enter valid Id";
		driver.findElement(By.xpath("//button[contains(@class,'primaryButton')]")).click();
		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(text(),'Invalid UPI')]")));
		String UImessage=driver.findElement(By.xpath("//p[contains(text(),'Invalid UPI')]")).getText(); 
		if(UImessage==expetedText)
		{
			System.out.println("Enter valid UPI ID");
		}
		System.out.println("E2E RedBus Booking is completed....!!!");
		driver.quit();
		

	}
	
	
	public static int daysToCurrentSat(int currentDay) {
		LocalDate today = LocalDate.now();
		LocalDate saturday = today.with(DayOfWeek.SATURDAY);
		int count = saturday.getDayOfMonth() - currentDay;
		if (count < 0) {
			return 7 + count;
		}
		return count;
	}
}
