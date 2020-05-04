// MIT License
// Copyright (c) Microsoft Corporation. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE

package com.azureblobstorage.app;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/* *************************************************************************************************************************
* Summary: This application demonstrates how to use the Blob Storage service.
* 
* *************************************************************************************************************************
*/
public class AzureApp {
	/*
	 * *****************************************************************************
	 * ******************************************** Instructions: Start an Azure
	 * storage emulator, such as Azurite, before running the app. Alternatively,
	 * remove the "UseDevelopmentStorage=true;"; string and uncomment the 3
	 * commented lines. Then, update the storageConnectionString variable with your
	 * AccountName and Key and run the sample.
	 * *****************************************************************************
	 * ********************************************
	 */

	public void getOpertionContext() {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyurl", 0000 /* port no */)); // setting the
																											// request
																											// specific
																											// proxy
		OperationContext op = new OperationContext();
		op.setProxy(proxy);
	}

	public static final String storageConnectionString = "DefaultEndpointsProtocol=https;" + "AccountName=accountnmae;"
			+ "AccountKey=keyvalue";

	public static CloudBlobContainer getCloudBlobContainer(String containerName)
			throws InvalidKeyException, URISyntaxException, StorageException {

		// Parse the connection string and create a blob client to interact with Blob
		// storage
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
		CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
		CloudBlobContainer container = blobClient.getContainerReference(containerName);

		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyurl", 0000 /* port no */));
		OperationContext.setDefaultProxy(proxy); // set default proxy if required to use in other domain

		return container;
	}

	public static void uploadFile(MultipartFile file, String container_name) throws IOException {
		System.out.println("Azure Blob storage start");
		File sourceFile = convert(file);

		try {
			CloudBlobContainer container = getCloudBlobContainer(container_name);
			// Create the container if it does not exist with public access.
			System.out.println("Creating container: " + container.getName());
			container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(),
					new OperationContext());

			if (sourceFile != null) {
				// Getting a blob reference
				CloudBlockBlob blob = container.getBlockBlobReference(sourceFile.getName());
				blob.getProperties().setContentType(file.getContentType());
				// Creating blob and uploading file to it
				System.out.println("Uploading the file ");
				blob.uploadFromFile(sourceFile.getAbsolutePath());
				System.out.println("URI of blob is: " + blob.getUri().toString());

			}

		} catch (StorageException ex) {
			System.out.println(String.format("Error returned from the service. Http code: %d and error code: %s",
					ex.getHttpStatusCode(), ex.getErrorCode()));
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			System.out.println("The program has completed successfully.");

		}
	}

	public static List<URI> getContainerBlobs(String containerName)
			throws InvalidKeyException, URISyntaxException, StorageException {
		CloudBlobContainer container = getCloudBlobContainer(containerName);
//		 Listing contents of container
		List<URI> list = new ArrayList<URI>();
		for (ListBlobItem blobItem : container.listBlobs()) {
			list.add(blobItem.getUri());
			System.out.println("URI of blob is: " + blobItem.getUri());

		}
		return list;
	}

	public static void downloadBlob(String uri, String container_name)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		CloudBlobContainer container = getCloudBlobContainer(container_name);

		URI blobUri = new URI(uri);

		CloudBlockBlob ref = new CloudBlockBlob(blobUri);
		// Getting a blob reference
		CloudBlockBlob blob = container.getBlockBlobReference(ref.getName());

		System.out.println("blob.getName() : " + ref.getName());
		File sourceFile = new File("C:\\Users\\username\\Desktop\\New folder\\" + ref.getName());
		System.out.println("sourceFile.getAbsolutePath() is: " + sourceFile.getAbsolutePath());

		/*
		 * Download the blob's content to output stream.
		 */

		blob.downloadToFile(sourceFile.getAbsolutePath());
		System.out.println("Downloaded ******************");

		/*
		 * OR
		 */

		/*
		 * *****************************************************************************
		 * ****************************** Download the blob's content to output stream.
		 */
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			blob.download(bos);
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * blob.getName(); ///file name blob.getProperties().getContentType(); // get
		 * the content type of blob bos.toByteArray(); // get the content of blob
		 * 
		 ***********************************************************************************************************/
	}

	// convert multipart file to File
	public static File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
}
